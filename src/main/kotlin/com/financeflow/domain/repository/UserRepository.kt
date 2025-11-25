package com.financeflow.domain.repository

import com.financeflow.domain.model.User
import java.util.*

interface UserRepository : Repository<User, UUID> {
    suspend fun findByEmail(email: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun updatePassword(userId: UUID, newPassword: String): Boolean
    suspend fun deactivate(userId: UUID): Boolean
}
