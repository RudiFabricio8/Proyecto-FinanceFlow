package com.financeflow.application.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class NotificationResponse(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val relatedEntityType: String?,
    val relatedEntityId: String?,
    val createdAt: Long,
    val readAt: Long?
)

@Serializable
data class NotificationCreateRequest(
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null
)

@Serializable
data class NotificationUpdateRequest(
    val isRead: Boolean? = null
)

@Serializable
data class NotificationFilter(
    val userId: String? = null,
    val isRead: Boolean? = null,
    val type: String? = null,
    val relatedEntityType: String? = null,
    val relatedEntityId: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

@Serializable
data class NotificationCountResponse(
    val total: Int,
    val unread: Int,
    val read: Int
)

@Serializable
data class MarkAsReadRequest(
    val notificationIds: List<String>,
    val markAllAsRead: Boolean = false
)
