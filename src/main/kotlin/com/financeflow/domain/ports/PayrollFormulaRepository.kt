package com.financeflow.domain.ports

import com.financeflow.domain.model.PayrollFormula
import java.util.UUID

interface PayrollFormulaRepository {
    suspend fun create(formula: PayrollFormula): PayrollFormula
    suspend fun findById(id: UUID): PayrollFormula?
    suspend fun findByOrganizationId(organizationId: UUID): List<PayrollFormula>
    suspend fun update(formula: PayrollFormula): PayrollFormula
    suspend fun delete(id: UUID)
}
