package com.financeflow

import com.financeflow.exception.configureExceptionHandling
import com.financeflow.routing.configureRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.path
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.koin.core.module.Module

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    com.financeflow.infrastructure.db.DatabaseConfig.init(environment.config)    
    install(Koin) {
        slf4jLogger()
        modules(koinModules)
    }

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        configureExceptionHandling()
    }

    routing {
    }

    configureRouting()

    logger.info(
        "Application started in environment: " +
                environment.config.propertyOrNull("ktor.deployment.environment")?.getString().orEmpty()
    )
}

val koinModules = listOf<Module>(

)