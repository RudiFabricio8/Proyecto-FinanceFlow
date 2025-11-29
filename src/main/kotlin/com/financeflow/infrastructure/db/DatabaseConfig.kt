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
    private lateinit var dataSource: HikariDataSource
    lateinit var database: Database
    private val isInitialized: Boolean get() = ::dataSource.isInitialized
    
    @Throws(IllegalStateException::class)
    fun init(config: ApplicationConfig) {
        if (isInitialized) {
            logger.warn("Database is already initialized")
            return
        }
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
        
        try {
            dataSource = HikariDataSource(dbConfig).apply {
                validate()
            }
            database = Database.connect(dataSource)
            logger.info("Database connection pool initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize database connection pool", e)
            dataSource.close()
            throw IllegalStateException("Failed to initialize database connection pool", e)
        }
    }

    @Throws(IllegalStateException::class)
    fun migrate() {
        if (!isInitialized) {
            throw IllegalStateException("Database not initialized. Call init() first.")
        }
        
        try {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .failOnMissingLocations(true)
                .validateMigrationNaming(true)
                .load()
            
            val migrations = flyway.info().all()
            logger.info("Found ${migrations.size} migrations")
            
            if (migrations.isNotEmpty()) {
                val result = flyway.migrate()
                
                if (result.migrationsExecuted > 0) {
                    logger.info("Successfully applied ${result.migrationsExecuted} database migration(s)")
                    migrations.takeLast(result.migrationsExecuted).forEach {
                        logger.info("Applied migration: ${it.version} - ${it.description}")
                    }
                } else {
                    logger.info("Database is up to date - no new migrations to apply")
                }
                
                // Validate after migration
                flyway.validate()
            } else {
                logger.warn("No migration scripts found in classpath:db/migration")
            }
        } catch (e: Exception) {
            logger.error("Failed to run database migrations", e)
            throw IllegalStateException("Failed to run database migrations", e)
        }
    }
    @Throws(IllegalStateException::class)
    fun clean() {
        if (!isInitialized) {
            throw IllegalStateException("Database not initialized. Call init() first.")
        }
        
        try {
            if (System.getenv("ENVIRONMENT") != "test") {
                logger.error("Clean operation is only allowed in test environment")
                throw IllegalStateException("Clean operation is only allowed in test environment")
            }
            
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
            
            logger.warn("Starting database clean operation - this will remove all data!")
            flyway.clean()
            logger.warn("Database cleaned - all data has been removed")
        } catch (e: Exception) {
            logger.error("Failed to clean database", e)
            throw IllegalStateException("Failed to clean database", e)
        }
    }
    

    @Synchronized
    fun close() {
        if (isInitialized) {
            try {
                if (!dataSource.isClosed) {
                    logger.info("Closing database connection pool...")
                    dataSource.close()
                    logger.info("Database connection pool closed successfully")
                }
            } catch (e: Exception) {
                logger.error("Error while closing database connection pool", e)
                throw e
            }
        } else {
            logger.warn("Attempted to close database connection pool, but it was not initialized")
        }
    }
}

fun Application.getDatabaseConfig(): ApplicationConfig {
    return this.environment.config.config("ktor.database")
}
