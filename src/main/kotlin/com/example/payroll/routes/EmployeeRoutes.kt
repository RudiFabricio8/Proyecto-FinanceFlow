package com.example.payroll.routes

import com.example.payroll.models.Employee
import com.example.payroll.services.EmployeeService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.employeeRoutes() {

    route("/employees") {

        // ✅ Obtener todos los empleados
        get {
            val employees = EmployeeService.getAll()
            call.respond(employees)
        }

        // ✅ Crear un nuevo empleado
        post {
            try {
                val employee = call.receive<Employee>()
                val created = EmployeeService.insert(employee)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
            }
        }
    }
}
