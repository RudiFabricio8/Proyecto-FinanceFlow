package com.financeflow.infrastructure.db

import org.ktorm.schema.*
import org.ktorm.schema.unique
import org.ktorm.schema.nullable
import java.util.*

object Users : Table<Nothing>("users") {
    val id = uuid("id").primaryKey()
    val email = varchar("email").unique()
    val password = varchar("password")
    val fullName = varchar("full_name")
    val role = varchar("role")
    val isActive = boolean("is_active").defaultTo(true)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Transactions : Table<Nothing>("transactions") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(Users) { it.id }
    val amount = decimal("amount")
    val type = varchar("type")
    val category = varchar("category")
    val description = varchar("description").nullable()
    val date = long("date")
    val reference = varchar("reference").nullable()
    val status = varchar("status")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Payrolls : Table<Nothing>("payrolls") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(Users) { it.id }
    val periodStart = long("period_start")
    val periodEnd = long("period_end")
    val baseSalary = decimal("base_salary")
    val netPay = decimal("net_pay")
    val status = varchar("status")
    val paymentDate = long("payment_date").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object PayrollDeductions : Table<Nothing>("payroll_deductions") {
    val id = uuid("id").primaryKey()
    val payrollId = uuid("payroll_id").references(Payrolls) { it.id }
    val name = varchar("name")
    val amount = decimal("amount")
    val type = varchar("type")
    val description = varchar("description").nullable()
}

object PayrollBonuses : Table<Nothing>("payroll_bonuses") {
    val id = uuid("id").primaryKey()
    val payrollId = uuid("payroll_id").references(Payrolls) { it.id }
    val name = varchar("name")
    val amount = decimal("amount")
    val description = varchar("description").nullable()
}

object Receipts : Table<Nothing>("receipts") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(Users) { it.id }
    val transactionId = uuid("transaction_id").references(Transactions) { it.id }.nullable()
    val amount = decimal("amount")
    val date = long("date")
    val category = varchar("category")
    val description = varchar("description").nullable()
    val attachmentUrl = varchar("attachment_url").nullable()
    val status = varchar("status")
    val reviewedBy = uuid("reviewed_by").references(Users) { it.id }.nullable()
    val reviewedAt = long("reviewed_at").nullable()
    val notes = text("notes").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

object Notifications : Table<Nothing>("notifications") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id").references(Users) { it.id }
    val title = varchar("title")
    val message = text("message")
    val type = varchar("type")
    val isRead = boolean("is_read").default(false)
    val relatedEntityType = varchar("related_entity_type").nullable()
    val relatedEntityId = uuid("related_entity_id").nullable()
    val createdAt = long("created_at")
    val readAt = long("read_at").nullable()
}
