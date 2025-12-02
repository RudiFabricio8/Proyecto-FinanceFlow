package com.financeflow.infrastructure.db

import com.financeflow.domain.model.DeductionType
import com.financeflow.domain.model.Payroll
import com.financeflow.domain.model.PayrollDeduction
import com.financeflow.domain.model.PayrollStatus
import com.financeflow.domain.repository.PayrollRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import java.util.UUID

class PayrollRepositoryImpl(private val database: Database) : PayrollRepository {

    // Helper para conversión de UUID (del modelo) a Long (de la BD)
    private fun UUID.toLongId(): Long = try {
        this.toString().split("-").first().toLong(16)
    } catch (e: Exception) {
        0L
    }

    // Helper para simular un UUID a partir de un Long de la BD
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

        val employeeId = 0L
        val payrollPeriodId = 0L

        val affectedRecords = database.update(Payslips) {
            set(it.employeeId, employeeId)
            set(it.payrollPeriodId, payrollPeriodId)
            set(it.grossSalary, entity.baseSalary)
            set(it.totalDeductions, 0.0)
            set(it.netSalary, entity.netPay)
            // 🚨 Corrección de tipo: asignando Long a la columna 'long'
            set(it.generatedAt, now)
            where { Payslips.id eq longId }
        }

        val payrollId: UUID

        if (affectedRecords == 0) {
            val newLongId = database.insertAndGenerateKey(Payslips) {
                set(it.employeeId, employeeId)
                set(it.payrollPeriodId, payrollPeriodId)
                set(it.grossSalary, entity.baseSalary)
                set(it.totalDeductions, 0.0)
                set(it.netSalary, entity.netPay)
                // 🚨 Corrección de tipo: asignando Long a la columna 'long'
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

    override suspend fun findByUserId(userId: UUID): List<Payroll> = emptyList()

    override suspend fun findByUserIdAndStatus(userId: UUID, status: PayrollStatus): List<Payroll> = emptyList()


    // --- Funciones Auxiliares ---

    private fun getPayslipDeductions(payslipId: UUID): List<PayrollDeduction> {
        val longId = payslipId.toLongId()

        return database.from(PayslipDeductions)
            .select()
            .where { PayslipDeductions.payslipId eq longId }
            .map { it.toPayslipDeduction() }
    }

    private suspend fun savePayslipDeductions(payslipId: UUID, deductions: List<PayrollDeduction>) {
        val longId = payslipId.toLongId()

        database.delete(PayslipDeductions) { PayslipDeductions.payslipId eq longId }

        deductions.forEach { deduction ->
            database.insert(PayslipDeductions) {
                set(it.payslipId, longId)
                set(it.deductionTypeId, 0L)
                set(it.amount, deduction.amount)
            }
        }
    }

    private fun QueryRowSet.toPayroll(): Payroll {
        val longId = this[Payslips.id]!!
        val payslipId = longId.toUuid()

        return Payroll(
            id = payslipId,
            userId = longId.toUuid(),
            periodStart = 0L,
            periodEnd = 0L,
            baseSalary = this[Payslips.grossSalary]!!.toDouble(),
            deductions = getPayslipDeductions(payslipId),
            bonuses = emptyList(),
            // 🚨 SOLUCIÓN FINAL: Usar la referencia completa del enum
            status = com.financeflow.domain.model.PayrollStatus.COMPLETED,
            netPay = this[Payslips.netSalary]!!.toDouble(),
            paymentDate = 0L,
            createdAt = this[Payslips.generatedAt]!!, // Mapeado a Long
            updatedAt = this[Payslips.generatedAt]!!  // Mapeado a Long
        )
    }

    private fun QueryRowSet.toPayslipDeduction(): PayrollDeduction {
        return PayrollDeduction(
            id = this[PayslipDeductions.id]!!.toUuid(),
            name = "Deducción Desconocida",
            amount = this[PayslipDeductions.amount]!!.toDouble(),
            type = DeductionType.OTHER,
            description = null
        )
    }
}