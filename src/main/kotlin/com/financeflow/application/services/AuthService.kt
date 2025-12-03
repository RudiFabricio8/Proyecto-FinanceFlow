package com.financeflow.application.services

import com.financeflow.domain.model.Organization
import com.financeflow.domain.model.User
import com.financeflow.domain.ports.OrganizationRepository
import com.financeflow.domain.ports.UserRepository
import com.financeflow.domain.valueobject.UserRole
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository
) {
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        organizationName: String
    ): Pair<User, Organization> {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("User with this email already exists")
        }
        
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        val now = Instant.now()
        
        val user = User(
            email = email,
            passwordHash = hashedPassword,
            fullName = fullName,
            role = UserRole.ACCOUNTANT,
            createdAt = now,
            updatedAt = now
        )
        
        val createdUser = userRepository.create(user)
        
        val organization = Organization(
            name = organizationName,
            taxId = null,
            ownerId = createdUser.id,
            address = null,
            phone = null,
            createdAt = now,
            updatedAt = now
        )
        
        val createdOrganization = organizationRepository.create(organization)
        
        return Pair(createdUser, createdOrganization)
    }

    suspend fun login(email: String, password: String): User? {
        val user = userRepository.findByEmail(email) ?: return null
        if (BCrypt.checkpw(password, user.passwordHash)) {
            return user
        }
        return null
    }
}
