package com.financeflow.adapters.rest

import com.financeflow.application.dto.*
import com.financeflow.application.services.AuthService
import com.financeflow.infrastructure.db.DatabaseConfig
import com.financeflow.infrastructure.db.UserRepositoryImpl
import com.financeflow.infrastructure.jwt.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.authRoutes(
    authService: AuthService = application.authService()
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }
        
        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }
        
        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val response = authService.refreshToken(request.refreshToken)
            call.respond(HttpStatusCode.OK, response)
        }
        
        authenticate {
            post("/change-password") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val request = call.receive<ChangePasswordRequest>()
                authService.changePassword(userId, request.currentPassword, request.newPassword)
                
                call.respond(HttpStatusCode.NoContent)
            }
            
            delete("/deactivate") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val request = call.receive<Map<String, String>>()
                val currentPassword = request["currentPassword"] 
                    ?: throw IllegalArgumentException("Current password is required")
                
                authService.deactivateAccount(userId, currentPassword)
                
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

fun Application.authService(): AuthService {
    val userRepository = UserRepositoryImpl(DatabaseConfig.database)
    val jwtConfig = JwtConfig(environment.config)
    return AuthService(userRepository, jwtConfig)
}
