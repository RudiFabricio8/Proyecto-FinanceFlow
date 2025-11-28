package com.financeflow

import com.financeflow.adapters.rest.configureRouting
import com.financeflow.config.OpenAPIConfig
import com.financeflow.infrastructure.configureAuth
import com.financeflow.infrastructure.configureDatabase
import com.financeflow.infrastructure.configureSerialization
import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.migrate
import com.financeflow.util.OpenAPIDocumentation
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import io.swagger.v3.oas.models.OpenAPI

fun main() {
    // Run database migrations before starting the server
    DatabaseConfig.migrate()
    
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
        // API documentation routes
        OpenAPIConfig().apply {
            configureOpenAPI()
        }
        
        // Application routes
        configureRouting()
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
