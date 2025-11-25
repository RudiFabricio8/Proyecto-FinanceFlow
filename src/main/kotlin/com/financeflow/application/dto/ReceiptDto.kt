package com.financeflow.application.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ReceiptCreateRequest(
    val transactionId: String? = null,
    val amount: Double,
    val category: String,
    val description: String? = null,
    val date: Long = System.currentTimeMillis()
)

@Serializable
data class ReceiptResponse(
    val id: String,
    val userId: String,
    val transactionId: String?,
    val amount: Double,
    val date: Long,
    val category: String,
    val description: String?,
    val attachmentUrl: String?,
    val status: String,
    val reviewedBy: String?,
    val reviewedAt: Long?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class ReceiptUpdateRequest(
    val category: String? = null,
    val description: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val reviewedBy: String? = null
)

@Serializable
data class ReceiptFilter(
    val userId: String? = null,
    val status: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val category: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

@Serializable
data class ReceiptUploadResponse(
    val id: String,
    val url: String,
    val filename: String,
    val size: Long,
    val mimeType: String
)
