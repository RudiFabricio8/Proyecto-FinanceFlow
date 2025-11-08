package com.example.payroll.database

import org.jetbrains.exposed.sql.Table

// Representa la tabla "employees" en tu base de datos
object EmployeesTable : Table("employees") {

    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val nss = varchar("nss_character", 50)
    val position = varchar("position_character", 100)
    val baseSalary = decimal("base_salary_numeric", 10, 2)
    val paymentFrequency = varchar("payment_frequency_character", 20)

    override val primaryKey = PrimaryKey(id)
}
