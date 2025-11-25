package com.financeflow.application.services

import com.financeflow.application.dto.*
import com.financeflow.domain.model.User
import com.financeflow.domain.model.UserRole
import com.financeflow.domain.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserService(private val userRepository: UserRepository) {
    
    suspend fun getUserProfile(userId: UUID): UserProfileResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        // In a real application, you would fetch additional stats here
        val stats = UserStats(
            totalTransactions = 0, // Would come from transaction service
            totalPayrolls = 0,     // Would come from payroll service
            totalReceipts = 0,     // Would come from receipt service
            unreadNotifications = 0 // Would come from notification service
        )
        
        return UserProfileResponse(
            id = user.id.toString(),
            email = user.email,
            fullName = user.fullName,
            role = user.role.name,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            stats = stats
        )
    }
    
    suspend fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll()
            .map { it.toResponse() }
    }
    
    suspend fun getUserById(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        return user.toResponse()
    }
    
    suspend fun createUser(request: UserCreateRequest): UserResponse {
        // Validate input
        require(request.email.isNotBlank()) { "Email is required" }
        require(request.password.length >= 8) { "Password must be at least 8 characters long" }
        require(request.fullName.isNotBlank()) { "Full name is required" }
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use")
        }
        
        // Create new user
        val user = User(
            email = request.email.lowercase(),
            password = request.password, // Will be hashed in repository
            fullName = request.fullName,
            role = request.role?.let { UserRole.valueOf(it) } ?: UserRole.USER
        )
        
        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }
    
    suspend fun updateUser(userId: UUID, request: UserUpdateRequest): UserResponse {
        // Find existing user
        val existingUser = userRepository.findById(userId)
            ?: throw NoSuchElementException("User not found with ID: $userId")
        
        // Update fields if provided
        val updatedUser = existingUser.copy(
            email = request.email?.lowercase() ?: existingUser.email,
            fullName = request.fullName ?: existingUser.fullName,
            role = request.role?.let { UserRole.valueOf(it) } ?: existingUser.role,
            isActive = request.isActive ?: existingUser.isActive
        )
        
        // Save updated user
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toResponse()
    }
    
    suspend fun deleteUser(userId: UUID) {
        // In a real application, you would also need to handle related data
        // (e.g., transactions, payrolls, etc.) or implement soft delete
        
        val deleted = userRepository.delete(userId)
        if (!deleted) {
            throw NoSuchElementException("User not found with ID: $userId")
        }
    }
    
    private fun User.toResponse(): UserResponse {
        return UserResponse(
            id = this.id.toString(),
            email = this.email,
            fullName = this.fullName,
            role = this.role.name,
            isActive = this.isActive,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
