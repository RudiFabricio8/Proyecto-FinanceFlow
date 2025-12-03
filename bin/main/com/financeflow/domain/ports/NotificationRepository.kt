package com.financeflow.domain.ports

import com.financeflow.domain.model.Notification
import java.util.UUID

interface NotificationRepository {
    suspend fun create(notification: Notification): Notification
    suspend fun findById(id: UUID): Notification?
    suspend fun findByUserId(userId: UUID, isRead: Boolean? = null): List<Notification>
    suspend fun markAsRead(id: UUID): Notification?
    suspend fun markAllAsRead(userId: UUID)
    suspend fun countUnread(userId: UUID): Long
}
