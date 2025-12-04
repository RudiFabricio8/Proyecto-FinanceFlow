package com.financeflow.domain.service

import com.financeflow.domain.model.Notification
import com.financeflow.domain.ports.NotificationRepository
import com.financeflow.domain.valueobject.NotificationSeverity
import com.financeflow.domain.valueobject.NotificationType
import java.time.Instant
import java.util.UUID

class NotificationService(private val notificationRepository: NotificationRepository) {

    suspend fun createDocumentUploadSuccess(userId: UUID, documentId: UUID, fileName: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.DOCUMENT_UPLOADED,
            title = "Documento subido exitosamente",
            message = "El archivo '$fileName' se ha procesado correctamente.",
            severity = NotificationSeverity.SUCCESS,
            relatedEntityType = "DOCUMENT",
            relatedEntityId = documentId,
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }

    suspend fun createDocumentUploadError(userId: UUID, fileName: String, error: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.DOCUMENT_ERROR,
            title = "Error al subir documento",
            message = "No se pudo procesar '$fileName': $error",
            severity = NotificationSeverity.ERROR,
            relatedEntityType = "DOCUMENT",
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }

    suspend fun createFormulaCalculationError(userId: UUID, formulaId: UUID, error: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.FORMULA_EXECUTED,
            title = "Error en cálculo de fórmula",
            message = "La fórmula no pudo calcularse: $error",
            severity = NotificationSeverity.ERROR,
            relatedEntityType = "FORMULA_ERROR",
            relatedEntityId = formulaId,
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }

    suspend fun createPeriodApprovalNeeded(userId: UUID, periodId: UUID, periodName: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.PERIOD_STATUS_CHANGED,
            title = "Aprobación de período requerida",
            message = "El período '$periodName' está pendiente de aprobación.",
            severity = NotificationSeverity.WARNING,
            relatedEntityType = "ADMIN_APPROVAL",
            relatedEntityId = periodId,
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }

    suspend fun createPayrollCalculationComplete(userId: UUID, periodId: UUID, periodName: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.CALCULATION_COMPLETED,
            title = "Nómina calculada",
            message = "La nómina para '$periodName' se ha completado.",
            severity = NotificationSeverity.SUCCESS,
            relatedEntityType = "PAYROLL_PERIOD",
            relatedEntityId = periodId,
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }

    suspend fun createGeneralError(userId: UUID, title: String, message: String): Notification {
        val notification = Notification(
            userId = userId,
            type = NotificationType.DOCUMENT_ERROR,
            title = title,
            message = message,
            severity = NotificationSeverity.ERROR,
            createdAt = Instant.now()
        )
        return notificationRepository.create(notification)
    }
}
