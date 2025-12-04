package com.financeflow

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.financeflow.plugins.*
import com.financeflow.di.appModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureDatabases()
    configureRouting()
}
