package com.financeflow.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }
        
        // TODO: Add your application routes here
        // Example:
        // userRoutes()
        // transactionRoutes()
    }
}

// Example route definitions
// private fun Route.userRoutes() {
//     route("/users") {
//         get {
//             // Handle GET /users
//             call.respondText("List of users")
//         }
//         
//         get("/{id}") {
//             // Handle GET /users/{id}
//             val id = call.parameters["id"]
//             call.respondText("User $id")
//         }
//     }
// }
