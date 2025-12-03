package com.financeflow.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PayrollCalculation(
    val id: UUID = UUID.randomUUID(),
    val formulaId: UUID? = null,
    val payrollPeriodId: UUID,
    val concept: String,
    val inputValues: Map<String, Any>,
    val resultAmount: BigDecimal,
    val calculationSteps: List<String>? = null,
    val calculatedBy: UUID,
    val calculatedAt: Instant? = null
)
