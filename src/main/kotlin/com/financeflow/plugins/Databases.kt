package com.financeflow.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.financeflow.infrastructure.persistence.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun Application.configureDatabases() {
    // Lectura de configuración con tus valores por defecto
    val dbUrl = environment.config.propertyOrNull("db.jdbcUrl")?.getString() ?: "jdbc:postgresql://localhost:5432/financeflow"
    val dbUser = environment.config.propertyOrNull("db.user")?.getString() ?: "postgres"
    val dbPassword = environment.config.propertyOrNull("db.password")?.getString() ?: "RFMaJa775"
    val dbDriver = environment.config.propertyOrNull("db.driver")?.getString() ?: "org.postgresql.Driver"

    val config = HikariConfig().apply {
        driverClassName = dbDriver
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

//    transaction {
//        SchemaUtils.create(
//            Users,
//            Organizations,
//            PayrollPeriods,
//            Documents,
//            PayrollFormulas,
//            PayrollCalculations,
//            Notifications,
//            AuditLogs
//        )
//    }
}