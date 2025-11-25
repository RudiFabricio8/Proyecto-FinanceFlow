package com.financeflow.domain.model

import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val password: String,
    val fullName: String,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    USER, ADMIN, ACCOUNTANT
}

data class UserCredentials(
    val email: String,
    val password: String
)
