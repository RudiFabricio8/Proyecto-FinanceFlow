package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.model.Notification
import com.financeflow.domain.ports.NotificationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.UUID

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class DismissNotificationResponse(
    val message: String,
    val notification: NotificationResponse
)

@Serializable
data class NotificationsListResponse(
    val items: List<NotificationResponse>,
    val unreadCount: Long
)

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    val isRead: Boolean,
    val navigationPath: String?,
    val createdAt: String?
)

fun Route.notificationRoutes() {
    val notificationRepository by inject<NotificationRepository>()

    route("/notifications") {
        authenticate("auth-jwt") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val isRead = call.request.queryParameters["isRead"]?.toBoolean()
                
                val notifications = notificationRepository.findByUserId(UUID.fromString(userId), isRead)
                call.respond(NotificationsListResponse(
                    items = notifications.map { it.toResponse() },
                    unreadCount = notificationRepository.countUnread(UUID.fromString(userId))
                ))
            }

            patch("/{id}/read") {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val updated = notificationRepository.markAsRead(UUID.fromString(id))
                    ?: return@patch call.respond(HttpStatusCode.NotFound, ErrorResponse("Notification not found"))
                
                call.respond(updated.toResponse())
            }

            patch("/{id}/dismiss") {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorResponse("id required"))
                
                val notification = notificationRepository.markAsRead(UUID.fromString(id))
                if (notification == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Notification not found"))
                } else {
                    call.respond(HttpStatusCode.OK, DismissNotificationResponse(
                        message = "Notification dismissed",
                        notification = notification.toResponse()
                    ))
                }
            }

            patch("/read-all") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                notificationRepository.markAllAsRead(UUID.fromString(userId))
                call.respond(HttpStatusCode.OK, MessageResponse("All notifications marked as read"))
            }
        }
    }
}

private fun Notification.toResponse(): NotificationResponse {
    val navigationPath = when (relatedEntityType) {
        "DOCUMENT" -> "/receipts"
        "PAYROLL_PERIOD", "PAYROLL_FORMULA", "FORMULA_ERROR" -> "/payroll"
        "ADMIN_APPROVAL", "COMPLIANCE" -> "/admin"
        else -> null
    }
    
    return NotificationResponse(
        id = id.toString(),
        type = type.name,
        title = title,
        message = message,
        severity = severity.name,
        isRead = isRead,
        navigationPath = navigationPath,
        createdAt = createdAt?.toString()
    )
}
