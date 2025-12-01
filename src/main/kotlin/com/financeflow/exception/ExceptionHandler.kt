package com.financeflow.exception

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ExceptionHandler")

fun StatusPagesConfig.configureExceptionHandling() {
    exception<Throwable> { call, cause ->
        when (cause) {
            is IllegalArgumentException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "status" to HttpStatusCode.BadRequest.value,
                        "message" to (cause.message ?: "Invalid request"),
                        "error" to cause.toString()
                    )
                )
            }
            is NoSuchElementException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf(
                        "status" to HttpStatusCode.NotFound.value,
                        "message" to (cause.message ?: "Resource not found"),
                        "error" to cause.toString()
                    )
                )
            }
            is SecurityException -> {
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf(
                        "status" to HttpStatusCode.Forbidden.value,
                        "message" to (cause.message ?: "Forbidden"),
                        "error" to cause.toString()
                    )
                )
            }
            is RequestValidationException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "status" to HttpStatusCode.BadRequest.value,
                        "message" to "Validation failed",
                        "errors" to cause.reasons
                    )
                )
            }
            is BadRequestException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf(
                        "status" to HttpStatusCode.BadRequest.value,
                        "message" to (cause.message ?: "Bad request"),
                        "error" to cause.toString()
                    )
                )
            }
            else -> {
                logger.error("Unhandled exception", cause)
                call.application.environment.log.error("Unhandled exception", cause)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        status = HttpStatusCode.InternalServerError.value,
                        message = "Internal server error",
                        details = emptyMap()
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
