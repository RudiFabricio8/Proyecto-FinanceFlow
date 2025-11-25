package com.financeflow.application.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PayrollCreateRequest(
    val userId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val baseSalary: Double,
    val deductions: List<PayrollDeductionDto> = emptyList(),
    val bonuses: List<PayrollBonusDto> = emptyList()
)

@Serializable
data class PayrollResponse(
    val id: String,
    val userId: String,
    val periodStart: Long,
    val periodEnd: Long,
    val baseSalary: Double,
    val deductions: List<PayrollDeductionDto>,
    val bonuses: List<PayrollBonusDto>,
    val netPay: Double,
    val status: String,
    val paymentDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class PayrollDeductionDto(
    val name: String,
    val amount: Double,
    val type: String,
    val description: String? = null
)

@Serializable
data class PayrollBonusDto(
    val name: String,
    val amount: Double,
    val description: String? = null
)

@Serializable
data class PayrollUpdateRequest(
    val status: String? = null,
    val paymentDate: Long? = null,
    val deductions: List<PayrollDeductionDto>? = null,
    val bonuses: List<PayrollBonusDto>? = null
)

@Serializable
data class PayrollFilter(
    val userId: String? = null,
    val status: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

@Serializable
data class PayrollSummary(
    val totalPayrolls: Int,
    val totalAmount: Double,
    val averageNetPay: Double,
    val statusBreakdown: Map<String, Int>
)
