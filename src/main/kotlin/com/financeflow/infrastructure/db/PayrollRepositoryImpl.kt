package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Payroll
import com.financeflow.domain.model.PayrollStatus
import com.financeflow.domain.repository.PayrollRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.util.*

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
            set(it.userId, entity.userId)
            set(it.periodStart, entity.periodStart)
            set(it.periodEnd, entity.periodEnd)
            set(it.baseSalary, entity.baseSalary)
            set(it.netPay, entity.netPay)
            set(it.status, entity.status.name)
            set(it.paymentDate, entity.paymentDate)
            set(it.updatedAt, now)
            
            where { it.id eq entity.id }
        }
        
        val payrollId = if (affectedRecords == 0) {
            // Insert new payroll
            val newId = UUID.randomUUID()
            
            database.insert(Payrolls) {
                set(it.id, newId)
                set(it.userId, entity.userId)
                set(it.periodStart, entity.periodStart)
                set(it.periodEnd, entity.periodEnd)
                set(it.baseSalary, entity.baseSalary)
                set(it.netPay, entity.netPay)
                set(it.status, entity.status.name)
                set(it.paymentDate, entity.paymentDate)
                set(it.createdAt, now)
                set(it.updatedAt, now)
            }
            
            // Save deductions and bonuses
            savePayrollDeductions(newId, entity.deductions)
            savePayrollBonuses(newId, entity.bonuses)
            
            newId
        } else {
            // Update existing payroll
            savePayrollDeductions(entity.id, entity.deductions)
            savePayrollBonuses(entity.id, entity.bonuses)
            entity.id
        }
        
        return findById(payrollId)!!
    }

    override suspend fun delete(id: UUID): Boolean {
        // Delete related records first
        database.delete(PayrollDeductions) { it.payrollId eq id }
        database.delete(PayrollBonuses) { it.payrollId eq id }
        
        // Then delete the payroll
        val affectedRows = database.delete(Payrolls) { it.id eq id }
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
        // Delete existing deductions
        database.delete(PayrollDeductions) { it.payrollId eq payrollId }
        
        // Insert new deductions
        deductions.forEach { deduction ->
            database.insert(PayrollDeductions) {
                set(it.id, UUID.randomUUID())
                set(it.payrollId, payrollId)
                set(it.name, deduction.name)
                set(it.amount, deduction.amount)
                set(it.type, deduction.type.name)
                set(it.description, deduction.description)
            }
        }
    }

    private suspend fun savePayrollBonuses(payrollId: UUID, bonuses: List<PayrollBonus>) {
        // Delete existing bonuses
        database.delete(PayrollBonuses) { it.payrollId eq payrollId }
        
        // Insert new bonuses
        bonuses.forEach { bonus ->
            database.insert(PayrollBonuses) {
                set(it.id, UUID.randomUUID())
                set(it.payrollId, payrollId)
                set(it.name, bonus.name)
                set(it.amount, bonus.amount)
                set(it.description, bonus.description)
            }
        }
    }

    private fun QueryRowSet.toPayroll(): Payroll {
        return Payroll(
            id = this[Payrolls.id]!!,
            userId = this[Payrolls.userId]!!,
            periodStart = this[Payrolls.periodStart]!!,
            periodEnd = this[Payrolls.periodEnd]!!,
            baseSalary = this[Payrolls.baseSalary]!!.toDouble(),
            deductions = emptyList(), // Will be populated separately
            bonuses = emptyList(),    // Will be populated separately
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
