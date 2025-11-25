package com.financeflow.domain.model

import java.util.*

data class Payroll(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val periodStart: Long,
    val periodEnd: Long,
    val baseSalary: Double,
    val deductions: List<PayrollDeduction> = emptyList(),
    val bonuses: List<PayrollBonus> = emptyList(),
    val netPay: Double,
    val status: PayrollStatus = PayrollStatus.PENDING,
    val paymentDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PayrollDeduction(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val amount: Double,
    val type: DeductionType,
    val description: String? = null
)

data class PayrollBonus(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val amount: Double,
    val description: String? = null
)

enum class PayrollStatus {
    DRAFT, PENDING, PAID, CANCELLED
}

enum class DeductionType {
    TAX, SOCIAL_SECURITY, HEALTH_INSURANCE, RETIREMENT, OTHER
}
