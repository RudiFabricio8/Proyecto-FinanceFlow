package com.financeflow.infrastructure.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger("DatabaseConfig")

object DatabaseConfig {
    private lateinit var dataSource: DataSource
    lateinit var database: Database
    
    fun init(config: ApplicationConfig) {
        // Configure HikariCP connection pool
        val dbConfig = HikariConfig().apply {
            driverClassName = config.property("ktor.database.driver").getString()
            jdbcUrl = config.property("ktor.database.url").getString()
            username = config.property("ktor.database.user").getString()
            password = config.property("ktor.database.password").getString()
            maximumPoolSize = config.property("ktor.database.maxPoolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            validate()
        }
        
        dataSource = HikariDataSource(dbConfig)
        database = Database.connect(dataSource)
        
        logger.info("Database connection pool initialized")
    }
    
    /**
     * Run database migrations using Flyway
     */
    fun migrate() {
        try {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
            
            val migrations = flyway.info().all()
            logger.info("Found ${migrations.size} migrations")
            
            val result = flyway.migrate()
            
            if (result.migrationsExecuted > 0) {
                logger.info("Applied ${result.migrationsExecuted} database migration(s)")
                migrations.takeLast(result.migrationsExecuted).forEach {
                    logger.info("Applied migration: ${it.version} - ${it.description}")
                }
            } else {
                logger.info("No new migrations to apply")
            }
        } catch (e: Exception) {
            logger.error("Failed to run database migrations", e)
            throw IllegalStateException("Failed to run database migrations", e)
        }
    }
    
    /**
     * Clean the database (for testing purposes only)
     */
    fun clean() {
        try {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
            
            flyway.clean()
            logger.warn("Database cleaned - all data has been removed")
        } catch (e: Exception) {
            logger.error("Failed to clean database", e)
            throw IllegalStateException("Failed to clean database", e)
        }
    }
    
    /**
     * Close the database connection pool
     */
    fun close() {
        if (::dataSource.isInitialized) {
            (dataSource as HikariDataSource).close()
            logger.info("Database connection pool closed")
        }
    }
}

/**
 * Extension function to get database configuration from Application
 */
fun Application.getDatabaseConfig(): ApplicationConfig {
    return this.environment.config.config("ktor.database")
}
