package com.example.payroll

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.callloging.*
import com.example.payroll.database.DatabaseFactory
import com.example.payroll.routes.employeeRoutes
import com.example.payroll.routes.employeeDocumentRoutes

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {

    // ✅ Inicializa la conexión con la base de datos
    DatabaseFactory.init()

    // ✅ Configura JSON y CORS
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
    }

    install(CallLogging)

    // ✅ Define las rutas
    routing {
        employeeRoutes()
        employeeDocumentRoutes()
    }
}
