package com.financeflow

import com.financeflow.adapters.rest.configureRouting
import com.financeflow.infrastructure.configureAuth
import com.financeflow.infrastructure.configureDatabase
import com.financeflow.infrastructure.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabase()
    configureAuth()
    configureRouting()
}
