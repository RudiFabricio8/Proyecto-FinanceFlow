package com.financeflow.application.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class UserCreateRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val role: String? = "USER"
)

@Serializable
data class UserUpdateRequest(
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class UserFilter(
    val email: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null,
    val search: String? = null
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val stats: UserStats? = null
)

@Serializable
data class UserStats(
    val totalTransactions: Int,
    val totalPayrolls: Int,
    val totalReceipts: Int,
    val unreadNotifications: Int
)
