package com.financeflow.domain.model

import java.time.Instant
import java.util.UUID

data class Organization(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val taxId: String?,
    val ownerId: UUID,
    val address: String?,
    val phone: String?,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
