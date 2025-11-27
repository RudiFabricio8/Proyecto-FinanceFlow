package com.financeflow.di

import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.PayrollRepositoryImpl
import com.financeflow.infrastructure.db.TransactionRepositoryImpl
import com.financeflow.infrastructure.db.UserRepositoryImpl
import com.financeflow.infrastructure.db.Tables
import com.financeflow.infrastructure.jwt.JwtConfig
import com.financeflow.application.services.AuthService
import com.financeflow.application.services.PayrollService
import com.financeflow.application.services.TransactionService
import com.financeflow.application.services.UserService
import com.financeflow.domain.repository.PayrollRepository
import com.financeflow.domain.repository.TransactionRepository
import com.financeflow.domain.repository.UserRepository
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

fun Application.configureKoin() {
    koin {
        modules(appModule)
    }
}

val appModule = module {
    // Database
    single { DatabaseConfig.database }
    
    // JWT
    single { JwtConfig(environment.config) }
    
    // Repositories
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<PayrollRepository> { PayrollRepositoryImpl(get()) }
    
    // Services
    single { AuthService(get(), get()) }
    single { UserService(get()) }
    single { TransactionService(get()) }
    single { PayrollService(get(), get()) }
}

// Extension functions for Koin components
fun Application.getAuthService(): AuthService = getKoin().get()
fun Application.getUserService(): UserService = getKoin().get()
fun Application.getTransactionService(): TransactionService = getKoin().get()
fun Application.getPayrollService(): PayrollService = getKoin().get()
