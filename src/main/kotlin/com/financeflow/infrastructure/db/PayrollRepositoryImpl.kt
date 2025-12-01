package com.financeflow.infrastructure.db

import com.financeflow.domain.model.DeductionType
import com.financeflow.domain.model.Payroll
import com.financeflow.domain.model.PayrollBonus
import com.financeflow.domain.model.PayrollDeduction
import com.financeflow.domain.model.PayrollStatus
import com.financeflow.domain.repository.PayrollRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import java.util.UUID

class PayrollRepositoryImpl(private val database: Database) : PayrollRepository {

    override suspend fun findAll(): List<Payroll> {
        val payrolls = database.from(Payrolls)
            .select()
            .map { it.toPayroll() }

        return payrolls.map { payroll ->
            val deductions = getPayrollDeductions(payroll.id)
            val bonuses = getPayrollBonuses(payroll.id)
            payroll.copy(deductions = deductions, bonuses = bonuses)
        }
    }

    override suspend fun findById(id: UUID): Payroll? {
        val payroll = database.from(Payrolls)
            .select()
            .where { Payrolls.id eq id }
            .map { it.toPayroll() }
            .firstOrNull()
            ?: return null

        val deductions = getPayrollDeductions(id)
        val bonuses = getPayrollBonuses(id)

        return payroll.copy(deductions = deductions, bonuses = bonuses)
    }

    override suspend fun save(entity: Payroll): Payroll {
        val now = System.currentTimeMillis()

        val affectedRecords = database.update(Payrolls) {
            set(Payrolls.userId, entity.userId)
            set(Payrolls.periodStart, entity.periodStart)
            set(Payrolls.periodEnd, entity.periodEnd)
            set(Payrolls.baseSalary, entity.baseSalary)   // Double
            set(Payrolls.netPay, entity.netPay)           // Double
            set(Payrolls.status, entity.status.name)
            set(Payrolls.paymentDate, entity.paymentDate)
            set(Payrolls.updatedAt, now)
            where { Payrolls.id eq entity.id }
        }

        val payrollId = if (affectedRecords == 0) {
            val newId = UUID.randomUUID()

            database.insert(Payrolls) {
                set(Payrolls.id, newId)
                set(Payrolls.userId, entity.userId)
                set(Payrolls.periodStart, entity.periodStart)
                set(Payrolls.periodEnd, entity.periodEnd)
                set(Payrolls.baseSalary, entity.baseSalary)
                set(Payrolls.netPay, entity.netPay)
                set(Payrolls.status, entity.status.name)
                set(Payrolls.paymentDate, entity.paymentDate)
                set(Payrolls.createdAt, now)
                set(Payrolls.updatedAt, now)
            }

            savePayrollDeductions(newId, entity.deductions)
            savePayrollBonuses(newId, entity.bonuses)

            newId
        } else {
            savePayrollDeductions(entity.id, entity.deductions)
            savePayrollBonuses(entity.id, entity.bonuses)
            entity.id
        }

        return findById(payrollId)!!
    }

    override suspend fun delete(id: UUID): Boolean {
        database.delete(PayrollDeductions) { PayrollDeductions.payrollId eq id }
        database.delete(PayrollBonuses) { PayrollBonuses.payrollId eq id }

        val affectedRows = database.delete(Payrolls) { Payrolls.id eq id }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        return database.from(Payrolls)
            .select(Payrolls.id)
            .where { Payrolls.id eq id }
            .totalRecords > 0
    }

    override suspend fun findByUserId(userId: UUID): List<Payroll> {
        val payrolls = database.from(Payrolls)
            .select()
            .where { Payrolls.userId eq userId }
            .orderBy(Payrolls.periodEnd.desc())
            .map { it.toPayroll() }

        return payrolls.map { payroll ->
            val deductions = getPayrollDeductions(payroll.id)
            val bonuses = getPayrollBonuses(payroll.id)
            payroll.copy(deductions = deductions, bonuses = bonuses)
        }
    }

    override suspend fun findByUserIdAndStatus(userId: UUID, status: PayrollStatus): List<Payroll> {
        val payrolls = database.from(Payrolls)
            .select()
            .where {
                (Payrolls.userId eq userId) and
                        (Payrolls.status eq status.name)
            }
            .orderBy(Payrolls.periodEnd.desc())
            .map { it.toPayroll() }

        return payrolls.map { payroll ->
            val deductions = getPayrollDeductions(payroll.id)
            val bonuses = getPayrollBonuses(payroll.id)
            payroll.copy(deductions = deductions, bonuses = bonuses)
        }
    }

    private fun getPayrollDeductions(payrollId: UUID): List<PayrollDeduction> {
        return database.from(PayrollDeductions)
            .select()
            .where { PayrollDeductions.payrollId eq payrollId }
            .map { it.toPayrollDeduction() }
    }

    private fun getPayrollBonuses(payrollId: UUID): List<PayrollBonus> {
        return database.from(PayrollBonuses)
            .select()
            .where { PayrollBonuses.payrollId eq payrollId }
            .map { it.toPayrollBonus() }
    }

    private suspend fun savePayrollDeductions(payrollId: UUID, deductions: List<PayrollDeduction>) {
        database.delete(PayrollDeductions) { PayrollDeductions.payrollId eq payrollId }

        deductions.forEach { deduction ->
            database.insert(PayrollDeductions) {
                set(PayrollDeductions.id, UUID.randomUUID())
                set(PayrollDeductions.payrollId, payrollId)
                set(PayrollDeductions.name, deduction.name)
                set(PayrollDeductions.amount, deduction.amount)   // Double
                set(PayrollDeductions.type, deduction.type.name)
                set(PayrollDeductions.description, deduction.description)
            }
        }
    }

    private suspend fun savePayrollBonuses(payrollId: UUID, bonuses: List<PayrollBonus>) {
        database.delete(PayrollBonuses) { PayrollBonuses.payrollId eq payrollId }

        bonuses.forEach { bonus ->
            database.insert(PayrollBonuses) {
                set(PayrollBonuses.id, UUID.randomUUID())
                set(PayrollBonuses.payrollId, payrollId)
                set(PayrollBonuses.name, bonus.name)
                set(PayrollBonuses.amount, bonus.amount)          // Double
                set(PayrollBonuses.description, bonus.description)
            }
        }
    }

    private fun QueryRowSet.toPayroll(): Payroll {
        val payrollId = this[Payrolls.id]!!
        return Payroll(
            id = payrollId,
            userId = this[Payrolls.userId]!!,
            periodStart = this[Payrolls.periodStart]!!,
            periodEnd = this[Payrolls.periodEnd]!!,
            baseSalary = this[Payrolls.baseSalary]!!.toDouble(),
            deductions = getPayrollDeductions(payrollId),
            bonuses = getPayrollBonuses(payrollId),
            netPay = this[Payrolls.netPay]!!.toDouble(),
            status = PayrollStatus.valueOf(this[Payrolls.status]!!),
            paymentDate = this[Payrolls.paymentDate],
            createdAt = this[Payrolls.createdAt]!!,
            updatedAt = this[Payrolls.updatedAt]!!
        )
    }

    private fun QueryRowSet.toPayrollDeduction(): PayrollDeduction {
        return PayrollDeduction(
            id = this[PayrollDeductions.id]!!,
            name = this[PayrollDeductions.name]!!,
            amount = this[PayrollDeductions.amount]!!.toDouble(),
            type = DeductionType.valueOf(this[PayrollDeductions.type]!!),
            description = this[PayrollDeductions.description]
        )
    }

    private fun QueryRowSet.toPayrollBonus(): PayrollBonus {
        return PayrollBonus(
            id = this[PayrollBonuses.id]!!,
            name = this[PayrollBonuses.name]!!,
            amount = this[PayrollBonuses.amount]!!.toDouble(),
            description = this[PayrollBonuses.description]
        )
    }
}
