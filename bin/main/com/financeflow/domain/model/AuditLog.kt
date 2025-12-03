package com.financeflow.domain.model

import java.time.Instant
import java.util.UUID

data class AuditLog(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID? = null,
    val action: String,
    val entityType: String? = null,
    val entityId: UUID? = null,
    val oldValue: Map<String, Any>? = null,
    val newValue: Map<String, Any>? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: Instant? = null
)
