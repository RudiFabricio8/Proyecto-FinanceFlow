package com.financeflow.application.services

import com.financeflow.application.dto.*
import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import com.financeflow.domain.repository.TransactionRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TransactionService(
    private val transactionRepository: TransactionRepository
) : KoinComponent {
    
    private val userService: UserService by inject()
    
    suspend fun createTransaction(userId: UUID, request: TransactionCreateRequest): TransactionResponse {
        // Validate user exists and is active
        userService.getUserById(userId)
        
        val transaction = Transaction(
            userId = userId,
            amount = request.amount,
            type = TransactionType.valueOf(request.type.uppercase()),
            category = request.category,
            description = request.description,
            date = request.date ?: System.currentTimeMillis(),
            reference = request.reference,
            status = TransactionStatus.PENDING
        )
        
        val savedTransaction = transactionRepository.save(transaction)
        return savedTransaction.toResponse()
    }
    
    suspend fun getTransactionById(transactionId: UUID, userId: UUID? = null): TransactionResponse {
        val transaction = transactionRepository.findById(transactionId)
            ?: throw NoSuchElementException("Transaction not found with ID: $transactionId")
        
        // If userId is provided, verify ownership
        userId?.let {
            if (transaction.userId != it) {
                throw SecurityException("Not authorized to access this transaction")
            }
        }
        
        return transaction.toResponse()
    }
    
    suspend fun updateTransaction(
        transactionId: UUID,
        userId: UUID,
        request: TransactionUpdateRequest
    ): TransactionResponse {
        val existing = transactionRepository.findById(transactionId)
            ?: throw NoSuchElementException("Transaction not found with ID: $transactionId")
        
        // Verify ownership
        if (existing.userId != userId) {
            throw SecurityException("Not authorized to update this transaction")
        }
        
        val updated = existing.copy(
            amount = request.amount ?: existing.amount,
            category = request.category ?: existing.category,
            description = request.description ?: existing.description,
            status = request.status?.let { TransactionStatus.valueOf(it.uppercase()) } ?: existing.status
        )
        
        val saved = transactionRepository.save(updated)
        return saved.toResponse()
    }
    
    suspend fun deleteTransaction(transactionId: UUID, userId: UUID) {
        val existing = transactionRepository.findById(transactionId)
            ?: throw NoSuchElementException("Transaction not found with ID: $transactionId")
        
        // Verify ownership
        if (existing.userId != userId) {
            throw SecurityException("Not authorized to delete this transaction")
        }
        
        transactionRepository.delete(transactionId)
    }
    
    suspend fun getUserTransactions(
        userId: UUID,
        page: Int = 1,
        pageSize: Int = 20,
        type: String? = null,
        status: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        category: String? = null
    ): PagedResponse<TransactionResponse> {
        // Validate user exists
        userService.getUserById(userId)
        
        // Build filters
        val transactions = transactionRepository.findByUserId(userId)
            .filter { transaction ->
                type?.let { transaction.type == TransactionType.valueOf(it.uppercase()) } ?: true
            }
            .filter { transaction ->
                status?.let { transaction.status == TransactionStatus.valueOf(it.uppercase()) } ?: true
            }
            .filter { transaction ->
                startDate?.let { transaction.date >= it } ?: true
            }
            .filter { transaction ->
                endDate?.let { transaction.date <= it } ?: true
            }
            .filter { transaction ->
                category?.let { transaction.category.equals(it, ignoreCase = true) } ?: true
            }
            .sortedByDescending { it.date }
        
        // Apply pagination
        val totalItems = transactions.size
        val totalPages = (totalItems + pageSize - 1) / pageSize
        val paginated = transactions
            .drop((page - 1) * pageSize)
            .take(pageSize)
            .map { it.toResponse() }
        
        return PagedResponse(
            data = paginated,
            page = page,
            pageSize = pageSize,
            totalItems = totalItems.toLong(),
            totalPages = totalPages
        )
    }
    
    suspend fun getTransactionSummary(
        userId: UUID,
        startDate: Long? = null,
        endDate: Long? = null
    ): TransactionSummary {
        // Get all user transactions within date range
        val transactions = transactionRepository.findByUserId(userId)
            .filter { transaction ->
                startDate?.let { transaction.date >= it } ?: true
            }
            .filter { transaction ->
                endDate?.let { transaction.date <= it } ?: true
            }
        
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME && it.status == TransactionStatus.COMPLETED }
            .sumOf { it.amount }
        
        val totalExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE && it.status == TransactionStatus.COMPLETED }
            .sumOf { it.amount }
        
        val categoryBreakdown = transactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        
        return TransactionSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netAmount = totalIncome - totalExpenses,
            categoryBreakdown = categoryBreakdown
        )
    }
    
    private fun Transaction.toResponse(): TransactionResponse {
        return TransactionResponse(
            id = this.id.toString(),
            amount = this.amount,
            type = this.type.name.lowercase(),
            category = this.category,
            description = this.description,
            date = this.date,
            reference = this.reference,
            status = this.status.name.lowercase(),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}

data class PagedResponse<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
)
