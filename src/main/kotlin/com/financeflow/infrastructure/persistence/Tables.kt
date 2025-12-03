package com.financeflow.infrastructure.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val role = varchar("role", 50).default("ACCOUNTANT")
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object Organizations : Table("organizations") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val taxId = varchar("tax_id", 100).nullable()
    val ownerId = reference("owner_id", Users.id)
    val address = text("address").nullable()
    val phone = varchar("phone", 50).nullable()
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object PayrollPeriods : Table("payroll_periods") {
    val id = uuid("id")
    val organizationId = reference("organization_id", Organizations.id)
    val name = varchar("name", 100)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val status = varchar("status", 50).default("DRAFT")
    val totalGrossSalary = decimal("total_gross_salary", 15, 2).default(java.math.BigDecimal.ZERO)
    val totalDeductions = decimal("total_deductions", 15, 2).default(java.math.BigDecimal.ZERO)
    val totalNetSalary = decimal("total_net_salary", 15, 2).default(java.math.BigDecimal.ZERO)
    val notes = text("notes").nullable()
    val createdBy = reference("created_by", Users.id)
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object Documents : Table("documents") {
    val id = uuid("id")
    val organizationId = reference("organization_id", Organizations.id)
    val payrollPeriodId = reference("payroll_period_id", PayrollPeriods.id).nullable()
    val fileName = varchar("file_name", 500)
    val filePath = varchar("file_path", 1000)
    val fileSizeBytes = long("file_size_bytes")
    val fileType = varchar("file_type", 100)
    val documentType = varchar("document_type", 100).nullable()
    val uploadStatus = varchar("upload_status", 50).default("UPLOADED")
    val parsingError = text("parsing_error").nullable()
    val extractedAmount = decimal("extracted_amount", 15, 2).nullable()
    val extractedDate = date("extracted_date").nullable()
    val uploadedBy = reference("uploaded_by", Users.id)
    val uploadedAt = timestamp("uploaded_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object PayrollFormulas : Table("payroll_formulas") {
    val id = uuid("id")
    val organizationId = reference("organization_id", Organizations.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val formulaExpression = text("formula_expression")
    val variables = text("variables").nullable() // JSON serialized
    val createdBy = reference("created_by", Users.id)
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object PayrollCalculations : Table("payroll_calculations") {
    val id = uuid("id")
    val formulaId = reference("formula_id", PayrollFormulas.id).nullable()
    val payrollPeriodId = reference("payroll_period_id", PayrollPeriods.id)
    val concept = varchar("concept", 255)
    val inputValues = text("input_values") // JSON serialized
    val resultAmount = decimal("result_amount", 15, 2)
    val calculationSteps = text("calculation_steps").nullable() // JSON serialized
    val calculatedBy = reference("calculated_by", Users.id)
    val calculatedAt = timestamp("calculated_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object Notifications : Table("notifications") {
    val id = uuid("id")
    val userId = reference("user_id", Users.id)
    val type = varchar("type", 100)
    val title = varchar("title", 255)
    val message = text("message")
    val severity = varchar("severity", 50).default("INFO")
    val isRead = bool("is_read").default(false)
    val relatedEntityType = varchar("related_entity_type", 100).nullable()
    val relatedEntityId = uuid("related_entity_id").nullable()
    val createdAt = timestamp("created_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object AuditLogs : Table("audit_logs") {
    val id = uuid("id")
    val userId = reference("user_id", Users.id).nullable()
    val action = varchar("action", 255)
    val entityType = varchar("entity_type", 100).nullable()
    val entityId = uuid("entity_id").nullable()
    val oldValue = text("old_value").nullable() // JSON serialized
    val newValue = text("new_value").nullable() // JSON serialized
    val ipAddress = varchar("ip_address", 50).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt = timestamp("created_at").nullable()
    
    override val primaryKey = PrimaryKey(id)
}
