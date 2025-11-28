package com.financeflow.application.services

import com.financeflow.application.dto.AuthResponse
import com.financeflow.application.dto.LoginRequest
import com.financeflow.application.dto.RefreshTokenRequest
import com.financeflow.application.dto.RegisterRequest
import com.financeflow.domain.model.User
import com.financeflow.domain.model.UserRole
import com.financeflow.domain.repository.UserRepository
import com.financeflow.infrastructure.jwt.JwtConfig
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class AuthService(
    private val userRepository: UserRepository,
    private val jwtConfig: JwtConfig
) {
    
    suspend fun register(request: RegisterRequest): AuthResponse {
        require(request.email.isNotBlank()) { "Email is required" }
        require(request.password.length >= 8) { "Password must be at least 8 characters long" }
        require(request.fullName.isNotBlank()) { "Full name is required" }
        
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already in use")
        }
        
        val user = User(
            email = request.email.lowercase(),
            password = request.password,
            fullName = request.fullName,
            role = UserRole.USER
        )
        
        val savedUser = userRepository.save(user)
        
        return generateAuthResponse(savedUser)
    }
    
    suspend fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email.lowercase())
            ?: throw IllegalArgumentException("Invalid email or password")
        
        if (!BCrypt.checkpw(request.password, user.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }
        
        if (!user.isActive) {
            throw IllegalStateException("Account is deactivated")
        }
        
        return generateAuthResponse(user)
    }
    
    suspend fun refreshToken(refreshToken: String): AuthResponse {
        val decodedJWT = jwtConfig.validateToken(refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")
        
        if (decodedJWT.subject != "Refresh") {
            throw IllegalArgumentException("Invalid token type")
        }
        
        val userId = UUID.fromString(decodedJWT.getClaim("userId").asString())
        
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        
        if (!user.isActive) {
            throw IllegalStateException("Account is deactivated")
        }
        
        return generateAuthResponse(user)
    }
    
    private fun generateAuthResponse(user: User): AuthResponse {
        val accessToken = jwtConfig.createAccessToken(user.id.toString(), user.role.name)
        val refreshToken = jwtConfig.createRefreshToken(user.id.toString())
        
        return AuthResponse(
            token = accessToken,
            refreshToken = refreshToken,
            userId = user.id.toString(),
            role = user.role.name
        )
    }
    
    suspend fun changePassword(userId: UUID, currentPassword: String, newPassword: String) {
        require(newPassword.length >= 8) { "New password must be at least 8 characters long" }
        
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        
        if (!BCrypt.checkpw(currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }
        
        userRepository.updatePassword(userId, newPassword)
    }
    
    suspend fun deactivateAccount(userId: UUID, currentPassword: String) {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")
        
        if (!BCrypt.checkpw(currentPassword, user.password)) {
            throw IllegalArgumentException("Password is incorrect")
        }
        
        userRepository.deactivate(userId)
    }
}
