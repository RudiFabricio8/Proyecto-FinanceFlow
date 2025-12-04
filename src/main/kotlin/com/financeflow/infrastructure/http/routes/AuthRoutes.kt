package com.financeflow.infrastructure.http.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.financeflow.application.services.AuthService
import com.financeflow.domain.ports.OrganizationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.Date

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val organizationName: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDTO,
    val organization: OrganizationDTO
)

@Serializable
data class UserDTO(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String
)

@Serializable
data class OrganizationDTO(
    val id: String,
    val name: String
)

fun Route.authRoutes() {
    val authService by inject<AuthService>()
    val jwtAudience = application.environment.config.propertyOrNull("jwt.audience")?.getString() ?: "http://0.0.0.0:8080/api"
    val jwtIssuer = application.environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "http://0.0.0.0:8080/"
    val jwtSecret = application.environment.config.propertyOrNull("jwt.secret")?.getString() ?: "financeflow-secret-key-change-me"

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            try {
                val (user, organization) = authService.register(
                    email = request.email,
                    password = request.password,
                    fullName = request.fullName,
                    organizationName = request.organizationName
                )

                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("userId", user.id.toString())
                    .withClaim("email", user.email)
                    .withClaim("role", user.role.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
                    .sign(Algorithm.HMAC256(jwtSecret))

                call.respond(HttpStatusCode.Created, AuthResponse(
                    token = token,
                    user = UserDTO(
                        id = user.id.toString(),
                        email = user.email,
                        fullName = user.fullName,
                        role = user.role.name
                    ),
                    organization = OrganizationDTO(
                        id = organization.id.toString(),
                        name = organization.name
                    )
                ))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to (e.message ?: "Error")))
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = authService.login(request.email, request.password)
            if (user != null) {
                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("userId", user.id.toString())
                    .withClaim("email", user.email)
                    .withClaim("role", user.role.name)
                    .withClaim("organizationId", user.organizationId?.toString() ?: "")
                    .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
                    .sign(Algorithm.HMAC256(jwtSecret))
                
                call.respond(AuthResponse(
                    token = token,
                    user = UserDTO(
                        id = user.id.toString(),
                        email = user.email,
                        fullName = user.fullName,
                        role = user.role.name
                    ),
                    organization = OrganizationDTO(
                        id = user.organizationId?.toString() ?: "",
                        name = ""
                    )
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
            }
        }
    }
}
