package com.financeflow.domain.model

import com.financeflow.domain.valueobject.UserRole
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID? = null,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole = UserRole.ACCOUNTANT,
    val isActive: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
