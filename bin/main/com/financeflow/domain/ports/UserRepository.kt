package com.financeflow.domain.ports

import com.financeflow.domain.model.User
import java.util.UUID

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun create(user: User): User
    suspend fun findById(id: UUID): User?
}
