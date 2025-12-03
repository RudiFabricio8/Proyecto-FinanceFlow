package com.financeflow.domain.model

import com.financeflow.domain.valueobject.PeriodStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class PayrollPeriod(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: PeriodStatus = PeriodStatus.DRAFT,
    val totalGrossSalary: BigDecimal = BigDecimal.ZERO,
    val totalDeductions: BigDecimal = BigDecimal.ZERO,
    val totalNetSalary: BigDecimal = BigDecimal.ZERO,
    val notes: String? = null,
    val createdBy: UUID,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
