package com.financeflow.domain.ports

import com.financeflow.domain.model.PayrollCalculation
import java.util.UUID

interface PayrollCalculationRepository {
    suspend fun create(calculation: PayrollCalculation): PayrollCalculation
    suspend fun findById(id: UUID): PayrollCalculation?
    suspend fun findByPeriodId(periodId: UUID): List<PayrollCalculation>
    suspend fun findByFormulaId(formulaId: UUID): List<PayrollCalculation>
}
