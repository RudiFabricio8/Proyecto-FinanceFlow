package com.financeflow

import com.financeflow.adapters.rest.configureRouting
import com.financeflow.config.OpenAPIConfig
import com.financeflow.infrastructure.configureAuth
import com.financeflow.infrastructure.configureDatabase
import com.financeflow.infrastructure.configureSerialization
import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.migrate
import com.financeflow.util.OpenAPIDocumentation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun main() {
    try {
        // Initialize application configuration
        val environment = applicationEngineEnvironment {
            module { module() }
            connector {
                host = System.getenv("HOST") ?: "0.0.0.0"
                port = (System.getenv("PORT") ?: "8080").toInt()
            }
        }
        
        // Run database migrations before starting the server
        runCatching {
            DatabaseConfig.migrate()
            logger.info("Database migrations completed successfully")
        }.onFailure { e ->
            logger.error("Failed to run database migrations", e)
            throw e
        }
        
        // Start the server
        embeddedServer(Netty, environment).start(wait = true)
        
    } catch (e: Exception) {
        logger.error("Application failed to start", e)
        System.exit(1)
    } finally {
        // Ensure resources are cleaned up
        runCatching { DatabaseConfig.close() }
            .onFailure { e -> logger.error("Error during shutdown", e) }
    }
}

private val logger = LoggerFactory.getLogger("Application")

fun Application.module() {
    // Configure logging
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    
    // Configure exception handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message ?: "Invalid request"))
                }
                is NoSuchElementException -> {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to cause.message ?: "Resource not found"))
                }
                else -> {
                    logger.error("Unhandled exception", cause)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error")
                    )
                }
            }
        }
    }
    
    // Configure OpenAPI documentation
    install(OpenAPI) {
        swaggerUI = true
        swaggerPath = "/swagger"
        openAPI = createOpenAPIDocumentation()
    }
    
    // Configure application modules
    configureSerialization()
    configureDatabase()
    configureAuth()
    
    // Setup routes
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
        
        // API documentation routes
        OpenAPIConfig().apply {
            configureOpenAPI()
        }
        
        // Application routes
        configureRouting()
    }
    
    // Add shutdown hook for cleanup
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info("Application is stopping...")
        DatabaseConfig.close()
    }
}

private fun createOpenAPIDocumentation(): OpenAPI {
    return OpenAPIDocumentation.createDocumentation {
        // Add security requirements
        addSecurityItem(OpenAPIDocumentation.securityRequirement)
        
        // Configure components
        components {
            addSecuritySchemes(
                "jwt_auth",
                io.swagger.v3.oas.models.security.SecurityScheme()
                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
            
            // Add schemas
            OpenAPIDocumentation().apply {
                addSchemas()
            }
        }
        
        // Add tags
        tags = listOf(
            io.swagger.v3.oas.models.tags.Tag().name("Authentication").description("Authentication endpoints"),
            io.swagger.v3.oas.models.tags.Tag().name("Users").description("User management endpoints"),
            io.swagger.v3.oas.models.tags.Tag().name("Transactions").description("Transaction management endpoints"),
            io.swagger.v3.oas.models.tags.Tag().name("Payroll").description("Payroll management endpoints")
        )
    }
}
