package com.financeflow.infrastructure.db

import com.financeflow.domain.model.DeductionType
import com.financeflow.domain.model.Payroll
import com.financeflow.domain.model.PayrollDeduction
import com.financeflow.domain.model.PayrollStatus
import com.financeflow.domain.repository.PayrollRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import java.math.BigDecimal
import java.util.UUID

class PayrollRepositoryImpl(private val database: Database) : PayrollRepository {

    private fun UUID.toLongId(): Long = try {
        this.toString().split("-").first().toLong(16)
    } catch (e: Exception) {
        0L
    }

    private fun Long.toUuid(): UUID = UUID.nameUUIDFromBytes(this.toString().toByteArray())

    override suspend fun findAll(): List<Payroll> {
        val payrolls = database.from(Payslips)
            .select()
            .map { it.toPayroll() }

        return payrolls.map { payroll ->
            val deductions = getPayslipDeductions(payroll.id)
            payroll.copy(deductions = deductions, bonuses = emptyList())
        }
    }

    override suspend fun findById(id: UUID): Payroll? {
        val longId = id.toLongId()

        val payroll = database.from(Payslips)
            .select()
            .where { Payslips.id eq longId }
            .map { it.toPayroll() }
            .firstOrNull()
            ?: return null

        val deductions = getPayslipDeductions(id)

        return payroll.copy(deductions = deductions, bonuses = emptyList())
    }

    override suspend fun save(entity: Payroll): Payroll {
        val now = System.currentTimeMillis()
        val longId = entity.id.toLongId()

        val employeeId = entity.userId.toLongId()
        val payrollPeriodId = 0L // Ajusta si tienes un ID real

        val grossSalary = BigDecimal.valueOf(entity.baseSalary)
        val netSalary = BigDecimal.valueOf(entity.netPay)
        val totalDeductions = entity.deductions.fold(BigDecimal.ZERO) { acc, d -> acc + BigDecimal.valueOf(d.amount) }

        val affectedRecords = database.update(Payslips) {
            set(it.employeeId, employeeId)
            set(it.payrollPeriodId, payrollPeriodId)
            set(it.grossSalary, grossSalary)
            set(it.totalDeductions, totalDeductions)
            set(it.netSalary, netSalary)
            set(it.generatedAt, now)
            where { Payslips.id eq longId }
        }

        val payrollId: UUID

        if (affectedRecords == 0) {
            val newLongId = database.insertAndGenerateKey(Payslips) {
                set(it.employeeId, employeeId)
                set(it.payrollPeriodId, payrollPeriodId)
                set(it.grossSalary, grossSalary)
                set(it.totalDeductions, totalDeductions)
                set(it.netSalary, netSalary)
                set(it.generatedAt, now)
            } as Long

            payrollId = newLongId.toUuid()
            savePayslipDeductions(payrollId, entity.deductions)
        } else {
            payrollId = entity.id
            savePayslipDeductions(entity.id, entity.deductions)
        }

        return findById(payrollId)!!
    }

    override suspend fun delete(id: UUID): Boolean {
        val longId = id.toLongId()
        database.delete(PayslipDeductions) { PayslipDeductions.payslipId eq longId }
        val affectedRows = database.delete(Payslips) { Payslips.id eq longId }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        val longId = id.toLongId()
        return database.from(Payslips)
            .select(Payslips.id)
            .where { Payslips.id eq longId }
            .totalRecords > 0
    }

    override suspend fun findByUserId(userId: UUID): List<Payroll> {
        val employeeId = userId.toLongId()
        val payrolls = database.from(Payslips)
            .select()
            .where { Payslips.employeeId eq employeeId }
            .map { it.toPayroll() }
        
        return payrolls.map { payroll ->
            val deductions = getPayslipDeductions(payroll.id)
            payroll.copy(deductions = deductions, bonuses = emptyList())
        }
    }

    override suspend fun findByUserIdAndStatus(userId: UUID, status: PayrollStatus): List<Payroll> {
        return findByUserId(userId).filter { it.status == status }
    }

    private fun getPayslipDeductions(payslipId: UUID): List<PayrollDeduction> {
        val longId = payslipId.toLongId()

        return database.from(PayslipDeductions)
            .leftJoin(DeductionTypes, on = PayslipDeductions.deductionTypeId eq DeductionTypes.id)
            .select()
            .where { PayslipDeductions.payslipId eq longId }
            .map {
                PayrollDeduction(
                    id = it[PayslipDeductions.id]!!.toUuid(),
                    name = it[DeductionTypes.name] ?: "Desconocida",
                    amount = it[PayslipDeductions.amount]!!.toDouble(),
                    type = DeductionType.OTHER,
                    description = null
                )
            }
    }

    private suspend fun savePayslipDeductions(payslipId: UUID, deductions: List<PayrollDeduction>) {
        val longId = payslipId.toLongId()
        database.delete(PayslipDeductions) { PayslipDeductions.payslipId eq longId }

        deductions.forEach { deduction ->
            database.insert(PayslipDeductions) {
                set(it.payslipId, longId)
                set(it.deductionTypeId, 0L) // Ajusta si tienes el ID real
                set(it.amount, BigDecimal.valueOf(deduction.amount))
            }
        }
    }

    private fun QueryRowSet.toPayroll(): Payroll {
        val longId = this[Payslips.id]!!
        val payslipId = longId.toUuid()

        return Payroll(
            id = payslipId,
            userId = this[Payslips.employeeId]!!.toUuid(),
            periodStart = 0L,
            periodEnd = 0L,
            baseSalary = this[Payslips.grossSalary]!!.toDouble(),
            deductions = emptyList(),
            bonuses = emptyList(),
            status = PayrollStatus.COMPLETED,
            netPay = this[Payslips.netSalary]!!.toDouble(),
            paymentDate = null,
            createdAt = this[Payslips.generatedAt]!!,
            updatedAt = this[Payslips.generatedAt]!!
        )
    }
}
