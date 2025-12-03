package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.PayrollFormula
import com.financeflow.domain.ports.PayrollFormulaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedPayrollFormulaRepository : PayrollFormulaRepository {
    override suspend fun create(formula: PayrollFormula): PayrollFormula = dbQuery {
        PayrollFormulas.insert {
            it[id] = formula.id
            it[organizationId] = formula.organizationId
            it[name] = formula.name
            it[description] = formula.description
            it[formulaExpression] = formula.formulaExpression
            it[variables] = formula.variables?.let { vars -> Json.encodeToString(vars) }
            it[createdBy] = formula.createdBy
            it[createdAt] = formula.createdAt
            it[updatedAt] = formula.updatedAt
        }
        formula
    }

    override suspend fun findById(id: UUID): PayrollFormula? = dbQuery {
        PayrollFormulas.select { PayrollFormulas.id eq id }
            .map(::resultRowToFormula)
            .singleOrNull()
    }

    override suspend fun findByOrganizationId(organizationId: UUID): List<PayrollFormula> = dbQuery {
        PayrollFormulas.select { PayrollFormulas.organizationId eq organizationId }
            .orderBy(PayrollFormulas.createdAt to SortOrder.DESC)
            .map(::resultRowToFormula)
    }

    override suspend fun update(formula: PayrollFormula): PayrollFormula = dbQuery {
        PayrollFormulas.update({ PayrollFormulas.id eq formula.id }) {
            it[name] = formula.name
            it[description] = formula.description
            it[formulaExpression] = formula.formulaExpression
            it[variables] = formula.variables?.let { vars -> Json.encodeToString(vars) }
            it[updatedAt] = formula.updatedAt
        }
        formula
    }

    override suspend fun delete(id: UUID): Unit = dbQuery {
        PayrollFormulas.deleteWhere { PayrollFormulas.id eq id }
    }

    private fun resultRowToFormula(row: ResultRow) = PayrollFormula(
        id = row[PayrollFormulas.id],
        organizationId = row[PayrollFormulas.organizationId],
        name = row[PayrollFormulas.name],
        description = row[PayrollFormulas.description],
        formulaExpression = row[PayrollFormulas.formulaExpression],
        variables = row[PayrollFormulas.variables]?.let { Json.decodeFromString(it) },
        createdBy = row[PayrollFormulas.createdBy],
        createdAt = row[PayrollFormulas.createdAt],
        updatedAt = row[PayrollFormulas.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
