package com.financeflow.exception

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.*

@Suppress("unused")
fun StatusPagesConfig.configure() {
    exception<Throwable> { call, cause ->
        when (cause) {
            is IllegalArgumentException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = cause.message ?: "Invalid request",
                        details = mapOf("error" to cause.toString())
                    )
                )
            }
            is NoSuchElementException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        status = HttpStatusCode.NotFound.value,
                        message = cause.message ?: "Resource not found",
                        details = mapOf("error" to cause.toString())
                    )
                )
            }
            is SecurityException -> {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(
                        status = HttpStatusCode.Forbidden.value,
                        message = cause.message ?: "Forbidden",
                        details = mapOf("error" to cause.toString())
                    )
                )
            }
            is RequestValidationException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = "Validation failed",
                        details = mapOf("errors" to cause.reasons)
                    )
                )
            }
            is BadRequestException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = cause.message ?: "Bad request",
                        details = mapOf("error" to cause.toString())
                    )
                )
            }
            else -> {
                call.application.environment.log.error("Unhandled exception", cause)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        status = HttpStatusCode.InternalServerError.value,
                        message = "Internal server error",
                        details = if (call.application.environment.development) {
                            mapOf(
                                "error" to cause.toString(),
                                "stackTrace" to cause.stackTraceToString()
                            )
                        } else {
                            emptyMap()
                        }
                    )
                )
            }
        }
    }
}

data class ErrorResponse(
    val status: Int,
    val message: String,
    val details: Map<String, Any?> = emptyMap()
)
