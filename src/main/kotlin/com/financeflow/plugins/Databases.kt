package com.financeflow.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.financeflow.infrastructure.persistence.*

fun Application.configureDatabases() {
    val dbUrl = environment.config.propertyOrNull("db.jdbcUrl")?.getString() ?: "jdbc:postgresql://localhost:5432/financeflow"
    val dbUser = environment.config.propertyOrNull("db.user")?.getString() ?: "postgres"
    val dbPassword = environment.config.propertyOrNull("db.password")?.getString() ?: "RFMaJa775"
    val dbDriver = environment.config.propertyOrNull("db.driver")?.getString() ?: "org.postgresql.Driver"

    Database.connect(
        url = dbUrl,
        driver = dbDriver,
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.create(
            Users,
            Organizations,
            PayrollPeriods,
            Documents,
            PayrollFormulas,
            PayrollCalculations,
            Notifications,
            AuditLogs
        )
    }
}