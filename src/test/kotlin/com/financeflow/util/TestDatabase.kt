package com.financeflow.util

import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.Tables
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

/**
 * Test database configuration using TestContainers
 */
object TestDatabase {
    private val postgres = PostgreSQLContainer("postgres:15-alpine")
    private lateinit var dataSource: DataSource
    private lateinit var database: Database
    
    /**
     * Initialize the test database
     */
    fun init() {
        // Start the PostgreSQL container
        postgres.start()
        
        // Configure HikariCP for the test database
        val config = HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            driverClassName = postgres.driverClassName
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
        
        dataSource = HikariDataSource(config)
        
        // Initialize Ktorm database
        database = Database.connect(dataSource)
        
        // Run migrations
        migrate()
    }
    
    /**
     * Run database migrations
     */
    private fun migrate() {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        
        flyway.migrate()
    }
    
    /**
     * Clear all data from the test database
     */
    fun clearDatabase() {
        // Get all tables and truncate them in the correct order to avoid foreign key violations
        val tables = listOf(
            "refresh_tokens",
            "payroll_deductions",
            "payroll_bonuses",
            "payrolls",
            "receipts",
            "transactions",
            "notifications",
            "users"
        )
        
        database.useConnection { connection ->
            connection.prepareStatement("SET session_replication_role = 'replica';").use { it.execute() }
            
            tables.forEach { table ->
                connection.prepareStatement("TRUNCATE TABLE $table CASCADE;").use { it.execute() }
            }
            
            connection.prepareStatement("SET session_replication_role = 'origin';").use { it.execute() }
        }
    }
    
    /**
     * Get the test database instance
     */
    fun getDatabase(): Database = database
    
    /**
     * Get a table from the database
     */
    inline fun <reified E : Any, T : Table<E>> getTable(table: T): Entity.Factory<E> {
        return database.sequenceOf(table)
    }
    
    /**
     * Close the test database
     */
    fun close() {
        if (::dataSource.isInitialized) {
            (dataSource as HikariDataSource).close()
        }
        postgres.stop()
    }
}
