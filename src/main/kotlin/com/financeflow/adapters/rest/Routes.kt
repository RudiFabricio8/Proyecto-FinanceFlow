package com.financeflow.adapters.rest

import io.ktor.server.routing.*

fun Route.apiRoutes() {
    route("/api/v1") {
        authRoutes()
        userRoutes()
        transactionRoutes()
        payrollRoutes()
    }
}
