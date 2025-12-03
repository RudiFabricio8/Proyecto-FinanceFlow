package com.financeflow.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.financeflow.infrastructure.http.routes.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("FinanceFlow Payroll API is running!")
        }
        authRoutes()
        organizationRoutes()
        payrollRoutes()
        formulaRoutes()
        documentRoutes()
        analyticsRoutes()
        adminRoutes()
        notificationRoutes()
        dashboardRoutes()
    }
}
