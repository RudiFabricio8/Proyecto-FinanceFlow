package com.financeflow.adapters.rest

import com.financeflow.application.dto.*
import com.financeflow.application.services.UserService
import com.financeflow.infrastructure.db.UserRepositoryImpl
import com.financeflow.infrastructure.db.DatabaseConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.userRoutes(
    userService: UserService = application.userService()
) {
    route("/users") {
        authenticate {
            get("/me") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val userProfile = userService.getUserProfile(userId)
                call.respond(HttpStatusCode.OK, userProfile)
            }
            
            put("/me") {
                val userId = call.principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
                    ?: throw IllegalStateException("User ID not found in principal")
                
                val request = call.receive<UserUpdateRequest>()
                val updatedUser = userService.updateUser(userId, request)
                call.respond(HttpStatusCode.OK, updatedUser)
            }
            
            authenticate("admin") {
                get {
                    val users = userService.getAllUsers()
                    call.respond(HttpStatusCode.OK, users)
                }
                
                get("/{id}") {
                    val userId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Invalid user ID")
                    
                    val user = userService.getUserById(userId)
                    call.respond(HttpStatusCode.OK, user)
                }
                
                post {
                    val request = call.receive<UserCreateRequest>()
                    val newUser = userService.createUser(request)
                    call.respond(HttpStatusCode.Created, newUser)
                }
                
                put("/{id}") {
                    val userId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Invalid user ID")
                    
                    val request = call.receive<UserUpdateRequest>()
                    val updatedUser = userService.updateUser(userId, request)
                    call.respond(HttpStatusCode.OK, updatedUser)
                }
                
                delete("/{id}") {
                    val userId = call.parameters["id"]?.let { UUID.fromString(it) }
                        ?: throw IllegalArgumentException("Invalid user ID")
                    
                    userService.deleteUser(userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

fun Application.userService(): UserService {
    val userRepository = UserRepositoryImpl(DatabaseConfig.database)
    return UserService(userRepository)
}
