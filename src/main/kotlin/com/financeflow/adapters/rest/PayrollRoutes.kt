package com.financeflow.adapters.rest

import com.financeflow.application.dto.*
import com.financeflow.application.services.PayrollService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.payrollRoutes() {
    val payrollService: PayrollService by inject()
    
    route("/payrolls") {
        authenticate {
            post {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val request = call.receive<PayrollCreateRequest>()
                val payroll = payrollService.createPayroll(request, requestedBy)
                call.respond(HttpStatusCode.Created, payroll)
            }
            
            get {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val userId = call.request.queryParameters["userId"]?.let { UUID.fromString(it) }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
                val status = call.request.queryParameters["status"]
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()
                
                val targetUserId = if (userId != null) {
                    val requester = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                        ?: throw IllegalStateException("User ID not found in principal")
                    
                    if (userId != requester) {
                        val isAdminOrAccountant = call.principal<UserIdPrincipal>()?.let { principal ->
                            principal.attributes[UserRole.ADMIN] == true || 
                            principal.attributes[UserRole.ACCOUNTANT] == true
                        } ?: false
                        
                        if (!isAdminOrAccountant) {
                            throw SecurityException("Not authorized to view these payrolls")
                        }
                    }
                    userId
                } else {
                    requestedBy
                }
                
                val payrolls = payrollService.getUserPayrolls(
                    userId = targetUserId,
                    requestedBy = requestedBy,
                    page = page,
                    pageSize = pageSize,
                    status = status,
                    year = year,
                    month = month
                )
                
                call.respond(HttpStatusCode.OK, payrolls)
            }
            
            get("/summary") {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val userId = call.request.queryParameters["userId"]?.let { UUID.fromString(it) }
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()
                
                userId?.let {
                    if (it != requestedBy) {
                        val isAdminOrAccountant = call.principal<UserIdPrincipal>()?.let { principal ->
                            principal.attributes[UserRole.ADMIN] == true || 
                            principal.attributes[UserRole.ACCOUNTANT] == true
                        } ?: false
                        
                        if (!isAdminOrAccountant) {
                            throw SecurityException("Not authorized to view this summary")
                        }
                    }
                }
                
                val summary = payrollService.getPayrollSummary(
                    userId = userId,
                    year = year,
                    month = month
                )
                
                call.respond(HttpStatusCode.OK, summary)
            }
            
            get("/{id}") {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val payrollId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid payroll ID")
                
                val payroll = payrollService.getPayrollById(payrollId, requestedBy)
                call.respond(HttpStatusCode.OK, payroll)
            }
            
            put("/{id}") {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val payrollId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid payroll ID")
                
                val request = call.receive<PayrollUpdateRequest>()
                val updated = payrollService.updatePayroll(payrollId, request, requestedBy)
                call.respond(HttpStatusCode.OK, updated)
            }
            
            delete("/{id}") {
                val requestedBy = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val payrollId = call.parameters["id"]?.let { UUID.fromString(it) }
                    ?: throw IllegalArgumentException("Invalid payroll ID")
                
                payrollService.deletePayroll(payrollId, requestedBy)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
