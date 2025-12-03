package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.Notification
import com.financeflow.domain.ports.NotificationRepository
import com.financeflow.domain.valueobject.NotificationSeverity
import com.financeflow.domain.valueobject.NotificationType
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedNotificationRepository : NotificationRepository {
    override suspend fun create(notification: Notification): Notification = dbQuery {
        Notifications.insert {
            it[id] = notification.id
            it[userId] = notification.userId
            it[type] = notification.type.name
            it[title] = notification.title
            it[message] = notification.message
            it[severity] = notification.severity.name
            it[isRead] = notification.isRead
            it[relatedEntityType] = notification.relatedEntityType
            it[relatedEntityId] = notification.relatedEntityId
            it[createdAt] = notification.createdAt
        }
        notification
    }

    override suspend fun findById(id: UUID): Notification? = dbQuery {
        Notifications.select { Notifications.id eq id }
            .map(::resultRowToNotification)
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID, isRead: Boolean?): List<Notification> = dbQuery {
        val query = if (isRead != null) {
            Notifications.select { 
                (Notifications.userId eq userId) and (Notifications.isRead eq isRead) 
            }
        } else {
            Notifications.select { Notifications.userId eq userId }
        }
        
        query.orderBy(Notifications.createdAt to SortOrder.DESC)
            .map(::resultRowToNotification)
    }

    override suspend fun markAsRead(id: UUID): Notification? = dbQuery {
        Notifications.update({ Notifications.id eq id }) {
            it[isRead] = true
        }
        findById(id)
    }

    override suspend fun markAllAsRead(userId: UUID) = dbQuery {
        Notifications.update({ Notifications.userId eq userId }) {
            it[isRead] = true
        }
        Unit
    }

    override suspend fun countUnread(userId: UUID): Long = dbQuery {
        Notifications.select { 
            (Notifications.userId eq userId) and (Notifications.isRead eq false) 
        }.count()
    }

    private fun resultRowToNotification(row: ResultRow) = Notification(
        id = row[Notifications.id],
        userId = row[Notifications.userId],
        type = NotificationType.valueOf(row[Notifications.type]),
        title = row[Notifications.title],
        message = row[Notifications.message],
        severity = NotificationSeverity.valueOf(row[Notifications.severity]),
        isRead = row[Notifications.isRead],
        relatedEntityType = row[Notifications.relatedEntityType],
        relatedEntityId = row[Notifications.relatedEntityId],
        createdAt = row[Notifications.createdAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
