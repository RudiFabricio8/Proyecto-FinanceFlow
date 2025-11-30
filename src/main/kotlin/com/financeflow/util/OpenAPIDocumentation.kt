package com.financeflow.util

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.*
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Operation

/**
 * OpenAPI documentation configuration for the FinanceFlow API.
 * This object provides utilities for generating OpenAPI documentation.
 */
object OpenAPIDocumentation {
    private const val DEFAULT_ERROR_MESSAGE = "An unexpected error occurred"
    
    
    
    /**
     * Creates a standard error response.
     * @param description Description of the error response
     * @return Configured ApiResponse
     */
    fun createErrorResponse(description: String = "Error response"): ApiResponse {
        return ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(
                        Schema<Any>().`$ref`("#/components/schemas/ErrorResponse")
                    )
                )
            )
    }
    
    /**
     * Creates an unauthorized error response.
     * @return Configured ApiResponse for 401 Unauthorized
     */
    fun createUnauthorizedResponse(): ApiResponse {
        return ApiResponse()
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
    }
    
    /**
     * Creates a standard success response with a schema reference.
     * @param description Description of the successful response
     * @param schemaName Name of the schema to reference
     * @param isArray Whether the response is an array of the schema
     * @return Configured ApiResponse
     */
    fun createSuccessResponse(
        description: String,
        schemaName: String,
        isArray: Boolean = false
    ): ApiResponse {
        val schema = if (isArray) {
            ArraySchema().items(Schema<Any>().`$ref`("#/components/schemas/$schemaName"))
        } else {
            Schema<Any>().`$ref`("#/components/schemas/$schemaName")
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
            
    private val errorResponse: ApiResponse
        get() = createErrorResponse("Internal Server Error")
    
    private val securityRequirement = SecurityRequirement().addList("jwt_auth")
    
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
    
    /**
     * Common error response schema.
     */
    @Schema(description = "Standard error response format")
    data class ErrorResponse(
        @field:Schema(description = "HTTP status code", example = "400")
        val status: Int,
        
        @field:Schema(description = "Error message", example = "Invalid request parameters")
        val message: String,
        
        @field:Schema(
            description = "Additional error details",
            nullable = true,
            example = "{ \"field\": \"email\", \"error\": \"must be a well-formed email address\" }"
        )
        val details: Map<String, Any>? = null
    )
    
    /**
     * Pagination metadata schema.
     */
    @Schema(description = "Pagination metadata")
    data class PaginationMetadata(
        @field:Schema(description = "Current page number", example = "1")
        val page: Int,
        
        @field:Schema(description = "Number of items per page", example = "20")
        val pageSize: Int,
        
        @field:Schema(description = "Total number of items", example = "100")
        val totalItems: Int,
        
        @field:Schema(description = "Total number of pages", example = "5")
        val totalPages: Int
    )
    
    /**
     * Adds common schemas to the OpenAPI components.
     */
    fun Components.addSchemas() {
        // Error response schema
        addSchemas(
            "ErrorResponse",
            ObjectSchema()
                .addProperties(
                    "status", 
                    IntegerSchema()
                        .description("HTTP status code")
                        .example(400)
                )
                .addProperties(
                    "message", 
                    StringSchema()
                        .description("Error message")
                        .example("Invalid request parameters")
                )
                .addProperties(
                    "details", 
                    ObjectSchema()
                        .description("Additional error details")
                        .nullable(true)
                )
                .required(listOf("status", "message"))
        )
        
        // Pagination metadata schema
        addSchemas(
            "PaginationMetadata",
            ObjectSchema()
                .addProperties("page", IntegerSchema().description("Current page number").example(1))
                .addProperties("pageSize", IntegerSchema().description("Number of items per page").example(20))
                .addProperties("totalItems", IntegerSchema().description("Total number of items").example(100))
                .addProperties("totalPages", IntegerSchema().description("Total number of pages").example(5))
                .required(listOf("page", "pageSize", "totalItems", "totalPages"))
        )
    }
    

    fun schemaRef(name: String): io.swagger.v3.oas.models.media.Schema<Any> = 
        io.swagger.v3.oas.models.media.Schema<Any>().`$ref`("#/components/schemas/$name")
    
    
    fun responseWithSchema(
        description: String,
        schemaName: String,
        isArray: Boolean = false,
        statusCode: String = "200"
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
