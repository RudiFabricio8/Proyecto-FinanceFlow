package com.financeflow.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme

class OpenAPIConfig {
    fun Application.configureOpenAPI() {
        install(CORS) {
            anyHost()
            allowCredentials = true
            allowNonSimpleContentTypes = true
            allowSameOrigin = true
            allowHeaders { true }
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }

        // Configure OpenAPI documentation
        val openApi = OpenAPI()
            .info(createOpenAPIInfo())
            .addServersItem(
                io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:8080")
                    .description("Development server")
            )
            .components(
                Components().addSecuritySchemes(
                    "jwt_auth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .`in`(SecurityScheme.In.HEADER)
                        .name("Authorization")
                )
            )
            .addSecurityItem(SecurityRequirement().addList("jwt_auth"))

        routing {
            get("/openapi.json") {
                call.respond(openApi)
            }
            
            swaggerUI(path = "swagger", apiUrl = "/openapi.json", api = "FinanceFlow API") {
                version = "4.15.5"
            }
            
            get("/") {
                call.respondRedirect("/swagger", permanent = true)
            }
        }
    }
    
    private fun createOpenAPIInfo(): Info {
        return Info()
            .title("FinanceFlow API")
            .description("""
                |# FinanceFlow API
                |
                |API documentation for the FinanceFlow application.
                |
                |## Authentication
                |Most endpoints require authentication. Use the `/auth/login` endpoint to get a JWT token.
                |
                |## Rate Limiting
                |API is rate limited to 1000 requests per hour per IP address.
                """.trimMargin())
            .version("1.0.0")
            .contact(
                Contact()
                    .name("FinanceFlow Support")
                    .email("support@financeflow.com")
            )
            .license(
                License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
            )
    }
}

fun Route.apiDocumentation() {
    route("/api-docs") {
    }
}
