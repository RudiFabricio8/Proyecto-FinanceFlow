package com.example.payroll.routes

import com.example.payroll.models.EmployeeDocument
import com.example.payroll.services.EmployeeDocumentService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.employeeDocumentRoutes() {

    route("/employee-documents") {

        // ✅ Obtener todos los documentos
        get {
            val documents = EmployeeDocumentService.getAll()
            call.respond(documents)
        }

        // ✅ Crear un nuevo documento
        post {
            try {
                val document = call.receive<EmployeeDocument>()
                val created = EmployeeDocumentService.insert(document)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
    }
}
