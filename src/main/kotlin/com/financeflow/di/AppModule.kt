package com.financeflow.di

import com.financeflow.application.services.AuthService
import com.financeflow.domain.ports.*
import com.financeflow.domain.service.DocumentParserService
import com.financeflow.infrastructure.persistence.*
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<UserRepository> { ExposedUserRepository() }
    single<OrganizationRepository> { ExposedOrganizationRepository() }
    single<PayrollPeriodRepository> { ExposedPayrollPeriodRepository() }
    single<DocumentRepository> { ExposedDocumentRepository() }
    single<PayrollFormulaRepository> { ExposedPayrollFormulaRepository() }
    single<PayrollCalculationRepository> { ExposedPayrollCalculationRepository() }
    single<NotificationRepository> { ExposedNotificationRepository() }
    
    // Services
    single { AuthService(get(), get()) }
    single { DocumentParserService() }
}
