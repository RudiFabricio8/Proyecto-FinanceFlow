package com.financeflow.adapters.rest

import com.financeflow.application.dto.*
import com.financeflow.application.services.TransactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.transactionRoutes() {
    val transactionService: TransactionService by inject()
    
    route("/transactions") {
        authenticate {
            // Create a new transaction
            post {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val request = call.receive<TransactionCreateRequest>()
                val transaction = transactionService.createTransaction(userId, request)
                call.respond(HttpStatusCode.Created, transaction)
            }
            
            // Get all transactions for current user with filters
            get {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                val type = call.request.queryParameters["type"]
                val status = call.request.queryParameters["status"]
                val startDate = call.request.queryParameters["startDate"]?.toLongOrNull()
                val endDate = call.request.queryParameters["endDate"]?.toLongOrNull()
                val category = call.request.queryParameters["category"]
                
                val transactions = transactionService.getUserTransactions(
                    userId = userId,
                    page = page,
                    pageSize = pageSize,
                    type = type,
                    status = status,
                    startDate = startDate,
                    endDate = endDate,
                    category = category
                )
                
                call.respond(HttpStatusCode.OK, transactions)
            }
            
            // Get transaction summary
            get("/summary") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val startDate = call.request.queryParameters["startDate"]?.toLongOrNull()
                val endDate = call.request.queryParameters["endDate"]?.toLongOrNull()
                
                val summary = transactionService.getTransactionSummary(
                    userId = userId,
                    startDate = startDate,
                    endDate = endDate
                )
                
                call.respond(HttpStatusCode.OK, summary)
            }
            
            // Get a specific transaction
            get("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val transactionId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid transaction ID")
                
                val transaction = transactionService.getTransactionById(transactionId, userId)
                call.respond(HttpStatusCode.OK, transaction)
            }
            
            // Update a transaction
            put("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val transactionId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid transaction ID")
                
                val request = call.receive<TransactionUpdateRequest>()
                val updated = transactionService.updateTransaction(transactionId, userId, request)
                call.respond(HttpStatusCode.OK, updated)
            }
            
            // Delete a transaction
            delete("/{id}") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val transactionId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid transaction ID")
                
                transactionService.deleteTransaction(transactionId, userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
