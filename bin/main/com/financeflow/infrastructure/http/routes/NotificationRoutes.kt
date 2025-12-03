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
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    val isRead: Boolean,
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
                call.respond(mapOf(
                    "items" to notifications.map { it.toResponse() },
                    "unreadCount" to notificationRepository.countUnread(UUID.fromString(userId))
                ))
            }

            patch("/{id}/read") {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val updated = notificationRepository.markAsRead(UUID.fromString(id))
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Notification not found"))
                
                call.respond(updated.toResponse())
            }

            patch("/read-all") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                notificationRepository.markAllAsRead(UUID.fromString(userId))
                call.respond(HttpStatusCode.OK, mapOf("message" to "All notifications marked as read"))
            }
        }
    }
}

private fun Notification.toResponse() = NotificationResponse(
    id = id.toString(),
    type = type.name,
    title = title,
    message = message,
    severity = severity.name,
    isRead = isRead,
    createdAt = createdAt?.toString()
)
