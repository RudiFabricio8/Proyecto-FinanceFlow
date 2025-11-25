package com.financeflow.domain.model

import java.util.*

data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val description: String? = null,
    val date: Long = System.currentTimeMillis(),
    val reference: String? = null,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}
