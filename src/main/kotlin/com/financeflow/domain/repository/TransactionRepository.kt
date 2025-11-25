package com.financeflow.domain.repository

import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import java.time.LocalDate
import java.util.*

interface TransactionRepository : Repository<Transaction, UUID> {
    suspend fun findByUserId(userId: UUID): List<Transaction>
    suspend fun findByUserIdAndType(userId: UUID, type: TransactionType): List<Transaction>
    suspend fun findByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction>
    
    suspend fun updateStatus(transactionId: UUID, status: TransactionStatus): Boolean
    suspend fun findByStatus(status: TransactionStatus): List<Transaction>
    suspend fun getTotalAmountByUserAndType(userId: UUID, type: TransactionType): Double
}
