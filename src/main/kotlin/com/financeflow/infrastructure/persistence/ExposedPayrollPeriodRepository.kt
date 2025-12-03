package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.PayrollPeriod
import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.valueobject.PeriodStatus
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedPayrollPeriodRepository : PayrollPeriodRepository {
    override suspend fun create(period: PayrollPeriod): PayrollPeriod = dbQuery {
        PayrollPeriods.insert {
            it[id] = period.id
            it[organizationId] = period.organizationId
            it[name] = period.name
            it[startDate] = period.startDate
            it[endDate] = period.endDate
            it[status] = period.status.name
            it[totalGrossSalary] = period.totalGrossSalary
            it[totalDeductions] = period.totalDeductions
            it[totalNetSalary] = period.totalNetSalary
            it[notes] = period.notes
            it[createdBy] = period.createdBy
            it[createdAt] = period.createdAt
            it[updatedAt] = period.updatedAt
        }
        period
    }

    override suspend fun findById(id: UUID): PayrollPeriod? = dbQuery {
        PayrollPeriods.select { PayrollPeriods.id eq id }
            .map(::resultRowToPeriod)
            .singleOrNull()
    }

    override suspend fun findByOrganizationId(organizationId: UUID): List<PayrollPeriod> = dbQuery {
        PayrollPeriods.select { PayrollPeriods.organizationId eq organizationId }
            .orderBy(PayrollPeriods.startDate to SortOrder.DESC)
            .map(::resultRowToPeriod)
    }

    override suspend fun findByStatus(organizationId: UUID, status: PeriodStatus): List<PayrollPeriod> = dbQuery {
        PayrollPeriods.select { 
            (PayrollPeriods.organizationId eq organizationId) and 
            (PayrollPeriods.status eq status.name) 
        }
            .map(::resultRowToPeriod)
    }

    override suspend fun update(period: PayrollPeriod): PayrollPeriod = dbQuery {
        PayrollPeriods.update({ PayrollPeriods.id eq period.id }) {
            it[name] = period.name
            it[startDate] = period.startDate
            it[endDate] = period.endDate
            it[status] = period.status.name
            it[totalGrossSalary] = period.totalGrossSalary
            it[totalDeductions] = period.totalDeductions
            it[totalNetSalary] = period.totalNetSalary
            it[notes] = period.notes
            it[updatedAt] = period.updatedAt
        }
        period
    }

    override suspend fun delete(id: UUID): Unit = dbQuery {
        PayrollPeriods.deleteWhere { PayrollPeriods.id eq id }
    }

    private fun resultRowToPeriod(row: ResultRow) = PayrollPeriod(
        id = row[PayrollPeriods.id],
        organizationId = row[PayrollPeriods.organizationId],
        name = row[PayrollPeriods.name],
        startDate = row[PayrollPeriods.startDate],
        endDate = row[PayrollPeriods.endDate],
        status = PeriodStatus.valueOf(row[PayrollPeriods.status]),
        totalGrossSalary = row[PayrollPeriods.totalGrossSalary],
        totalDeductions = row[PayrollPeriods.totalDeductions],
        totalNetSalary = row[PayrollPeriods.totalNetSalary],
        notes = row[PayrollPeriods.notes],
        createdBy = row[PayrollPeriods.createdBy],
        createdAt = row[PayrollPeriods.createdAt],
        updatedAt = row[PayrollPeriods.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
