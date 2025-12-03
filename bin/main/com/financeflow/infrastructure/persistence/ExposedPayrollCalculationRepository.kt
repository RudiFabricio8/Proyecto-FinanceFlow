package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.PayrollCalculation
import com.financeflow.domain.ports.PayrollCalculationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedPayrollCalculationRepository : PayrollCalculationRepository {
    override suspend fun create(calculation: PayrollCalculation): PayrollCalculation = dbQuery {
        PayrollCalculations.insert {
            it[id] = calculation.id
            it[formulaId] = calculation.formulaId
            it[payrollPeriodId] = calculation.payrollPeriodId
            it[concept] = calculation.concept
            it[inputValues] = Json.encodeToString(calculation.inputValues)
            it[resultAmount] = calculation.resultAmount
            it[calculationSteps] = calculation.calculationSteps?.let { steps -> Json.encodeToString(steps) }
            it[calculatedBy] = calculation.calculatedBy
            it[calculatedAt] = calculation.calculatedAt
        }
        calculation
    }

    override suspend fun findById(id: UUID): PayrollCalculation? = dbQuery {
        PayrollCalculations.select { PayrollCalculations.id eq id }
            .map(::resultRowToCalculation)
            .singleOrNull()
    }

    override suspend fun findByPeriodId(periodId: UUID): List<PayrollCalculation> = dbQuery {
        PayrollCalculations.select { PayrollCalculations.payrollPeriodId eq periodId }
            .orderBy(PayrollCalculations.calculatedAt to SortOrder.DESC)
            .map(::resultRowToCalculation)
    }

    override suspend fun findByFormulaId(formulaId: UUID): List<PayrollCalculation> = dbQuery {
        PayrollCalculations.select { PayrollCalculations.formulaId eq formulaId }
            .orderBy(PayrollCalculations.calculatedAt to SortOrder.DESC)
            .map(::resultRowToCalculation)
    }

    private fun resultRowToCalculation(row: ResultRow) = PayrollCalculation(
        id = row[PayrollCalculations.id],
        formulaId = row[PayrollCalculations.formulaId],
        payrollPeriodId = row[PayrollCalculations.payrollPeriodId],
        concept = row[PayrollCalculations.concept],
        inputValues = Json.decodeFromString(row[PayrollCalculations.inputValues]),
        resultAmount = row[PayrollCalculations.resultAmount],
        calculationSteps = row[PayrollCalculations.calculationSteps]?.let { Json.decodeFromString(it) },
        calculatedBy = row[PayrollCalculations.calculatedBy],
        calculatedAt = row[PayrollCalculations.calculatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
