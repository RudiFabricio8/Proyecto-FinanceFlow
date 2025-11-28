package com.financeflow.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.oas.models.parameters.*
import io.swagger.v3.oas.models.responses.*
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import java.util.*

object OpenAPIDocumentation {
    private val securityRequirement = SecurityRequirement().addList("jwt_auth")
    
    private val errorResponse: ApiResponse
        get() = ApiResponse()
            .description("Error response")
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        ObjectSchema()
                            .addProperties("status", IntegerSchema().example(400))
                            .addProperties("message", StringSchema().example("Error message"))
                            .addProperties("details", ObjectSchema())
                    )
                )
            )
    
    private val unauthorizedResponse: ApiResponse
        get() = ApiResponse()
            .description("Unauthorized")
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        ObjectSchema()
                            .addProperties("status", IntegerSchema().example(401))
                            .addProperties("message", StringSchema().example("Unauthorized"))
                    )
                )
            )
    
    private val forbiddenResponse: ApiResponse
        get() = ApiResponse()
            .description("Forbidden")
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        ObjectSchema()
                            .addProperties("status", IntegerSchema().example(403))
                            .addProperties("message", StringSchema().example("Forbidden"))
                    )
                )
            )
    
    private val notFoundResponse: ApiResponse
        get() = ApiResponse()
            .description("Not Found")
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        ObjectSchema()
                            .addProperties("status", IntegerSchema().example(404))
                            .addProperties("message", StringSchema().example("Resource not found"))
                    )
                )
            )
    
    fun createDocumentation(init: OpenAPI.() -> Unit): OpenAPI {
        val openAPI = OpenAPI()
            .info(
                io.swagger.v3.oas.models.info.Info()
                    .title("FinanceFlow API")
                    .description("API for FinanceFlow application")
                    .version("1.0.0")
                    .contact(
                        io.swagger.v3.oas.models.info.Contact()
                            .name("FinanceFlow Team")
                            .email("support@financeflow.com")
                    )
                    .license(
                        io.swagger.v3.oas.models.info.License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .addServersItem(
                Server()
                    .url("http://localhost:8080")
                    .description("Development server")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "jwt_auth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"")
                    )
            )
        
        openAPI.init()
        return openAPI
    }
    
    fun Operation.addStandardResponses(includeNotFound: Boolean = false) {
        responses.apply {
            addApiResponse("401", unauthorizedResponse)
            addApiResponse("403", forbiddenResponse)
            if (includeNotFound) {
                addApiResponse("404", notFoundResponse)
            }
            addApiResponse("500", errorResponse)
        }
    }
    
    fun Operation.addPaginationParameters() {
        addParametersItem(
            Parameter()
                .`in`("query")
                .name("page")
                .schema(IntegerSchema())
                .description("Page number (1-based)")
                .required(false)
        )
        addParametersItem(
            Parameter()
                .`in`("query")
                .name("pageSize")
                .schema(IntegerSchema())
                .description("Number of items per page")
                .required(false)
        )
    }
    
    fun Operation.addSecurity() {
        addSecurityItem(securityRequirement)
    }
    
    fun Components.addSchemas() {
        // Add common schemas here
        addSchemas(
            "ErrorResponse",
            ObjectSchema()
                .addProperties("status", IntegerSchema())
                .addProperties("message", StringSchema())
                .addProperties("details", ObjectSchema())
        )
        
        // Add other schemas as needed
    }
    
    // Helper function to create a schema reference
    fun schemaRef(name: String) = Schema<Any>().`$ref`("#/components/schemas/$name")
    
    // Helper function to create a response with a schema reference
    fun responseWithSchema(
        description: String,
        schemaName: String,
        isArray: Boolean = false
    ): ApiResponse {
        val schema = if (isArray) {
            ArraySchema().items(schemaRef(schemaName))
        } else {
            schemaRef(schemaName)
        }
        
        return ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(schema)
                )
            )
    }
}
