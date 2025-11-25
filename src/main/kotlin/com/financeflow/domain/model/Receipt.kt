package com.financeflow.domain.model

import java.util.*

data class Receipt(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val transactionId: UUID? = null,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val category: String,
    val description: String? = null,
    val attachmentUrl: String? = null,
    val status: ReceiptStatus = ReceiptStatus.PENDING,
    val reviewedBy: UUID? = null,
    val reviewedAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ReceiptStatus {
    PENDING, APPROVED, REJECTED
}
