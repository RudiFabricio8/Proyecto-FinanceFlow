package com.financeflow.infrastructure.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import javax.sql.DataSource

object DatabaseConfig {
    private lateinit var dataSource: DataSource
    lateinit var database: Database
    
    fun init(config: ApplicationConfig) {
        val dbConfig = HikariConfig().apply {
            driverClassName = config.property("ktor.database.driver").getString()
            jdbcUrl = config.property("ktor.database.url").getString()
            username = config.property("ktor.database.user").getString()
            password = config.property("ktor.database.password").getString()
            maximumPoolSize = config.property("ktor.database.maxPoolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        dataSource = HikariDataSource(dbConfig)
        database = Database.connect(dataSource)
    }
    
    fun close() {
        if (::dataSource.isInitialized) {
            (dataSource as HikariDataSource).close()
        }
    }
}
