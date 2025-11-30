package com.financeflow

import com.financeflow.config.OpenAPIConfig
import com.financeflow.infrastructure.configureAuth
import com.financeflow.infrastructure.configureDatabase
import com.financeflow.infrastructure.configureSerialization
import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.migrate
import com.financeflow.routing.configureRouting
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.callloging.CallLoggingConfig
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    try {
        startApplication()
    } catch (e: Exception) {
        logger.error("Application failed to start", e)
        System.exit(1)
    } finally {
        shutdownApplication()
    }
}

private fun startApplication() {
    val environment = applicationEngineEnvironment {
        module { module() }
        configureServer()
    }
    
    runDatabaseMigrations()
    startServer(environment)
}

private fun ApplicationEngineEnvironment.configureServer() {
    connector {
        host = System.getenv("HOST") ?: "0.0.0.0"
        port = (System.getenv("PORT") ?: "8080").toInt()
    }
}

private fun runDatabaseMigrations() {
    runCatching {
        DatabaseConfig.migrate()
        logger.info("Database migrations completed successfully")
    }.onFailure { e ->
        logger.error("Failed to run database migrations", e)
        throw e
    }
}

private fun startServer(environment: ApplicationEngineEnvironment) {
    embeddedServer(Netty, environment).start(wait = true)
}

private fun shutdownApplication() {
    runCatching { 
        DatabaseConfig.close() 
    }.onFailure { e -> 
        logger.error("Error during shutdown", e) 
    }
}

fun Application.module() {
    configureLogging()
    configureExceptionHandling()
    configureApplicationModules()
    configureOpenAPI()
    configureRouting()
    configureShutdownHook()
}

private fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

private fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to (cause.message ?: "Invalid request"))
            )
        }
        
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound, 
                mapOf("error" to (cause.message ?: "Resource not found"))
            )
        }
        
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
    }
}

private fun Application.configureApplicationModules() {
    configureSerialization()
    configureDatabase()
    configureAuth()
}

private fun Application.configureOpenAPI() {
    install(OpenAPI) {
        swaggerUI = true
        swaggerPath = "/swagger"
        openAPI = createOpenAPIDocumentation()
    }
    OpenAPIConfig().configureOpenAPI()
}

private fun Application.configureRouting() {
    routing {
        // Application routes
        configureRouting()
    }
}

private fun Application.configureShutdownHook() {
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info("Application is stopping...")
        DatabaseConfig.close()
    }
}

private fun createOpenAPIDocumentation(): OpenAPI {
    return OpenAPI().apply {
        info = io.swagger.v3.oas.models.info.Info()
            .title("FinanceFlow API")
            .description("API for FinanceFlow application")
            .version("1.0.0")
        
        // Add security scheme
        components = Components().apply {
            addSecuritySchemes(
                "bearerAuth",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .`in`(SecurityScheme.In.HEADER)
                    .name("Authorization")
            )
        }
        
        // Add security requirement
        addSecurityItem(SecurityRequirement().addList("bearerAuth"))
        
        // Add tags
        tags = listOf(
            io.swagger.v3.oas.models.tags.Tag()
                .name("Authentication")
                .description("Authentication endpoints"),
            io.swagger.v3.oas.models.tags.Tag()
                .name("Users")
                .description("User management endpoints"),
            io.swagger.v3.oas.models.tags.Tag()
                .name("Transactions")
                .description("Transaction management endpoints"),
            io.swagger.v3.oas.models.tags.Tag()
                .name("Payroll")
                .description("Payroll management endpoints")
        )
    }
}
