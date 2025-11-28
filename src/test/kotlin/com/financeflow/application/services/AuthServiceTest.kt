package com.financeflow.application.services

import com.financeflow.BaseTest
import com.financeflow.application.dto.AuthResponse
import com.financeflow.application.dto.LoginRequest
import com.financeflow.application.dto.RegisterRequest
import com.financeflow.domain.model.User
import com.financeflow.domain.repository.UserRepository
import com.financeflow.infrastructure.jwt.JwtConfig
import com.financeflow.util.TestDatabase
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.inject
import java.util.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthServiceTest : BaseTest() {

    private val testDatabase = TestDatabase.getDatabase()
    private val userRepository: UserRepository = mock()
    private val jwtConfig = JwtConfig(ApplicationConfig("application-test.conf"))
    private val authService = AuthService(userRepository, jwtConfig)

    @Test
    fun `register should create new user and return tokens`() = runTest {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User"
        )

        val expectedUser = User(
            id = UUID.randomUUID(),
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            passwordHash = "hashed_password",
            role = "USER",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } returns expectedUser

        // When
        val result = authService.register(request)

        // Then
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertEquals(expectedUser.id.toString(), result.userId)
        assertEquals(expectedUser.email, result.email)
        assertEquals(expectedUser.firstName, result.firstName)
        assertEquals(expectedUser.lastName, result.lastName)

        verify { userRepository.findByEmail(request.email) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `login should return tokens for valid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = "\$2a\$10\$XFD9z5v5J5U6X5X5X5X5Xe5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X" // bcrypt hash of "password123"

        val user = User(
            id = UUID.randomUUID(),
            email = email,
            firstName = "Test",
            lastName = "User",
            passwordHash = hashedPassword,
            role = "USER",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        every { userRepository.findByEmail(email) } returns user

        // When
        val result = authService.login(LoginRequest(email, password))

        // Then
        assertNotNull(result.accessToken)
        assertNotNull(result.refreshToken)
        assertEquals(user.id.toString(), result.userId)
        assertEquals(user.email, result.email)

        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `login should throw for invalid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val hashedPassword = "\$2a\$10\$XFD9z5v5J5U6X5X5X5X5Xe5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X" // bcrypt hash of "password123"

        val user = User(
            id = UUID.randomUUID(),
            email = email,
            firstName = "Test",
            lastName = "User",
            passwordHash = hashedPassword,
            role = "USER",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        every { userRepository.findByEmail(email) } returns user

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            authService.login(LoginRequest(email, password))
        }

        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `refreshToken should return new access token`() = runTest {
        // Given
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val refreshToken = jwtConfig.generateRefreshToken(userId, email)

        val user = User(
            id = userId,
            email = email,
            firstName = "Test",
            lastName = "User",
            passwordHash = "hashed_password",
            role = "USER",
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        every { userRepository.findById(userId) } returns user

        // When
        val result = authService.refreshToken(refreshToken)

        // Then
        assertNotNull(result.accessToken)
        assertNotEquals(refreshToken, result.refreshToken) // Should be a new refresh token
        assertEquals(userId.toString(), result.userId)
        assertEquals(email, result.email)

        verify { userRepository.findById(userId) }
    }
}