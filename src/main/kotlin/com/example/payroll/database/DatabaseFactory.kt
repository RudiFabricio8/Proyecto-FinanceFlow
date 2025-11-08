package com.example.payroll.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.payroll.database.EmployeesTable

object DatabaseFactory {

    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/payroll", // cambia por tu base
            driver = "org.postgresql.Driver",
            user = "postgres", // tu usuario
            password = "tu_contraseña" // tu password
        )

        // Crear tablas si no existen
        transaction {
            SchemaUtils.create(EmployeesTable)
        }
    }
}
