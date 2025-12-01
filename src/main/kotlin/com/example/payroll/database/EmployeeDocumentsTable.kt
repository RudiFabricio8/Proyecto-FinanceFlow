package com.example.payroll.database

import org.jetbrains.exposed.sql.Table

object EmployeeDocumentsTable : Table("employee_documents") {

    val id = long("id").autoIncrement()
    val employeeId = long("employee_id")
    val documentTypeId = long("document_type_id")
    val documentUrl = text("document_url_text")

    override val primaryKey = PrimaryKey(id)
}
