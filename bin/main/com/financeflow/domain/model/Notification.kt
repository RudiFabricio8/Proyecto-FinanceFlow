package com.financeflow.domain.model

import com.financeflow.domain.valueobject.NotificationSeverity
import com.financeflow.domain.valueobject.NotificationType
import java.time.Instant
import java.util.UUID

data class Notification(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val message: String,
    val severity: NotificationSeverity = NotificationSeverity.INFO,
    val isRead: Boolean = false,
    val relatedEntityType: String? = null,
    val relatedEntityId: UUID? = null,
    val createdAt: Instant? = null
)
