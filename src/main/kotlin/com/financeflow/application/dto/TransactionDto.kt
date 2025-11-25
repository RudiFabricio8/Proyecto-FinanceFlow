package com.financeflow.application.dto

import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class TransactionCreateRequest(
    val amount: Double,
    val type: String,
    val category: String,
    val description: String? = null,
    val reference: String? = null
)

@Serializable
data class TransactionResponse(
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String?,
    val date: Long,
    val reference: String?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class TransactionUpdateRequest(
    val amount: Double? = null,
    val category: String? = null,
    val description: String? = null,
    val status: String? = null
)

@Serializable
data class TransactionFilter(
    val types: List<String>? = null,
    val categories: List<String>? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val statuses: List<String>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

@Serializable
data class TransactionSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netAmount: Double,
    val categoryBreakdown: Map<String, Double>
)
