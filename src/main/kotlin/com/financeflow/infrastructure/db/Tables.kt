package com.financeflow.infrastructure.db

import org.ktorm.schema.*
import java.util.*

object Users : Table<Nothing>("users") {
    val id = uuid("id").primaryKey()
    val email = varchar("email")
    val password = varchar("password")
    val fullName = varchar("full_name")
    val role = varchar("role")
    val isActive = boolean("is_active")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Transactions : Table<Nothing>("transactions") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id")
    val amount = decimal("amount")
    val type = varchar("type")
    val category = varchar("category")
    val description = varchar("description")
    val date = long("date")
    val reference = varchar("reference")
    val status = varchar("status")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Payrolls : Table<Nothing>("payrolls") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id")
    val periodStart = long("period_start")
    val periodEnd = long("period_end")
    val baseSalary = decimal("base_salary")
    val netPay = decimal("net_pay")
    val status = varchar("status")
    val paymentDate = long("payment_date")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object PayrollDeductions : Table<Nothing>("payroll_deductions") {
    val id = uuid("id").primaryKey()
    val payrollId = uuid("payroll_id")
    val name = varchar("name")
    val amount = decimal("amount")
    val type = varchar("type")
    val description = varchar("description")
}

object PayrollBonuses : Table<Nothing>("payroll_bonuses") {
    val id = uuid("id").primaryKey()
    val payrollId = uuid("payroll_id")
    val name = varchar("name")
    val amount = decimal("amount")
    val description = varchar("description")
}

object Receipts : Table<Nothing>("receipts") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id")
    val transactionId = uuid("transaction_id")
    val amount = decimal("amount")
    val date = long("date")
    val category = varchar("category")
    val description = varchar("description")
    val attachmentUrl = varchar("attachment_url")
    val status = varchar("status")
    val reviewedBy = uuid("reviewed_by")
    val reviewedAt = long("reviewed_at")
    val notes = text("notes")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Notifications : Table<Nothing>("notifications") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id")
    val title = varchar("title")
    val message = text("message")
    val type = varchar("type")
    val isRead = boolean("is_read")
    val relatedEntityType = varchar("related_entity_type")
    val relatedEntityId = uuid("related_entity_id")
    val createdAt = long("created_at")
    val readAt = long("read_at")
}
