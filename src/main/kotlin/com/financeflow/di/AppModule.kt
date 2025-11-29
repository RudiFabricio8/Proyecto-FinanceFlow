package com.financeflow.di

import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.PayrollRepositoryImpl
import com.financeflow.infrastructure.db.TransactionRepositoryImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext
import com.financeflow.infrastructure.db.UserRepositoryImpl
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
    single { JwtConfig(get<Application>().environment.config) }
    
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
fun Application.getAuthService(): AuthService = GlobalContext.get().get()
fun Application.getUserService(): UserService = GlobalContext.get().get()
fun Application.getTransactionService(): TransactionService = GlobalContext.get().get()
fun Application.getPayrollService(): PayrollService = GlobalContext.get().get()
