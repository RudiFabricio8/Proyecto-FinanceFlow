package com.financeflow.domain.model

import java.util.*

data class Notification(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val relatedEntityType: String? = null,
    val relatedEntityId: UUID? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null
)

enum class NotificationType {
    INFO, WARNING, ERROR, SUCCESS, PAYROLL, TRANSACTION, RECEIPT, SYSTEM
}
