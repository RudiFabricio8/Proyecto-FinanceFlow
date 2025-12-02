package com.financeflow.infrastructure.db

import org.ktorm.schema.*
import java.sql.Date // Necesario para el tipo date()
import java.sql.Timestamp

// Tablas de Usuarios
object Users : Table<Nothing>("users") {
    val id = long("id").primaryKey()
    val fullName = varchar("full_name")
    val email = varchar("email") // Mapeo de citext a varchar
    val passwordHash = text("password_hash")
    val role = varchar("role")
    val isActive = boolean("is_active")
    val createdAt = long("created_at") // Mapeo de timestamp with time zone a Long
}

// Tablas de Empleados
object Employees : Table<Nothing>("employees") {
    val id = long("id").primaryKey()
    val userId = long("user_id")
    val rfc = varchar("rfc")
    val nss = varchar("nss")
    val position = varchar("position")
    val hireDate = date("hire_date") // Mapeo de date a java.sql.Date
    val baseSalary = decimal("base_salary")
    val status = varchar("status")
    val paymentFrequency = varchar("payment_frequency")
}

// Tablas de Deducciones
object DeductionTypes : Table<Nothing>("deduction_types") {
    val id = long("id").primaryKey()
    val code = varchar("code")
    val name = varchar("name")
    val percentage = decimal("percentage")
    val fixedAmount = decimal("fixed_amount")
    val isActive = boolean("is_active")
}

object EmployeeDeductions : Table<Nothing>("employee_deductions") {
    val id = long("id").primaryKey()
    val employeeId = long("employee_id")
    val deductionTypeId = long("deduction_type_id")
    val value = decimal("value")
    val effectiveFrom = date("effective_from")
    val effectiveTo = date("effective_to")
}

// Tablas de Nóminas (Payslips y Pay Periods)
object PayrollPeriods : Table<Nothing>("payroll_periods") {
    val id = long("id").primaryKey()
    val startDate = date("start_date")
    val endDate = date("end_date")
    val frequency = varchar("frequency")
    val status = varchar("status")
    val generatedAt = long("generated_at")
    val approvedBy = long("approved_by")
    val approvedAt = long("approved_at")
}

object Payslips : Table<Nothing>("payslips") {
    val id = long("id").primaryKey()
    val employeeId = long("employee_id")
    val payrollPeriodId = long("payroll_period_id")
    val grossSalary = decimal("gross_salary")
    val totalDeductions = decimal("total_deductions")
    val netSalary = decimal("net_salary")
    val pdfUrl = text("pdf_url")
    val consultedAt = long("consulted_at")
    val generatedAt = long("generated_at")
}

object PayslipDeductions : Table<Nothing>("payslip_deductions") {
    val id = long("id").primaryKey()
    val payslipId = long("payslip_id")
    val deductionTypeId = long("deduction_type_id")
    val amount = decimal("amount")
}

object PayrollInputs : Table<Nothing>("payroll_inputs") {
    val id = long("id").primaryKey()
    val employeeId = long("employee_id")
    val payrollPeriodId = long("payroll_period_id")
    val hoursWorked = decimal("hours_worked")
    val overtimeHours = decimal("overtime_hours")
    val otherInputs = text("other_inputs") // jsonb -> text
    val recordedAt = long("recorded_at")
}

// Tablas de Transacciones (Payment History)
object PaymentHistory : Table<Nothing>("payment_history") {
    val id = long("id").primaryKey()
    val employeeId = long("employee_id")
    val paymentDate = date("payment_date") // Mapeo de date a java.sql.Date
    val paymentAmount = decimal("payment_amount")
    val paymentStatus = varchar("payment_status")
    val createdAt = long("created_at")
}

// Tablas de Logs y Reportes
object AuditLogs : Table<Nothing>("audit_logs") {
    val id = long("id").primaryKey()
    val userId = long("user_id")
    val action = varchar("action")
    val entity = varchar("entity")
    val entityId = long("entity_id")
    val ip = varchar("ip") // Mapeando inet como String
    val createdAt = long("created_at")
}

object EmployeeChangeLog : Table<Nothing>("employee_change_log") {
    val id = long("id").primaryKey()
    val employeeId = long("employee_id")
    val changeType = varchar("change_type")
    val oldValue = text("old_value") // jsonb -> text
    val newValue = text("new_value") // jsonb -> text
    val changedAt = long("changed_at")
    val changedBy = long("changed_by")
}

object Reports : Table<Nothing>("reports") {
    val id = long("id").primaryKey()
    val reportType = varchar("report_type")
    val filters = text("filters") // jsonb -> text
    val format = varchar("format")
    val fileUrl = text("file_url")
    val createdBy = long("created_by")
    val createdAt = long("created_at")
}