package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.model.PayrollPeriod
import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.valueobject.PeriodStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Serializable
data class CreatePeriodRequest(
    val organizationId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
)

@Serializable
data class UpdatePeriodStatusRequest(
    val status: String
)

@Serializable
data class PeriodResponse(
    val id: String,
    val organizationId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val totalGrossSalary: String,
    val totalDeductions: String,
    val totalNetSalary: String,
    val notes: String?,
    val createdAt: String?
)

fun Route.payrollRoutes() {
    val periodRepository by inject<PayrollPeriodRepository>()

    route("/payroll-periods") {
        authenticate("auth-jwt") {
            post {
                val request = call.receive<CreatePeriodRequest>()
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                
                val period = PayrollPeriod(
                    organizationId = UUID.fromString(request.organizationId),
                    name = request.name,
                    startDate = LocalDate.parse(request.startDate),
                    endDate = LocalDate.parse(request.endDate),
                    notes = request.notes,
                    createdBy = UUID.fromString(userId),
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                val created = periodRepository.create(period)
                call.respond(HttpStatusCode.Created, created.toResponse())
            }

            get {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val periods = periodRepository.findByOrganizationId(UUID.fromString(organizationId))
                call.respond(periods.map { it.toResponse() })
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val period = periodRepository.findById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Period not found"))
                
                call.respond(period.toResponse())
            }

            patch("/{id}/status") {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val request = call.receive<UpdatePeriodStatusRequest>()
                val period = periodRepository.findById(UUID.fromString(id))
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Period not found"))
                
                val updated = period.copy(
                    status = PeriodStatus.valueOf(request.status),
                    updatedAt = Instant.now()
                )
                
                periodRepository.update(updated)
                call.respond(updated.toResponse())
            }
        }
    }
}

private fun PayrollPeriod.toResponse() = PeriodResponse(
    id = id.toString(),
    organizationId = organizationId.toString(),
    name = name,
    startDate = startDate.toString(),
    endDate = endDate.toString(),
    status = status.name,
    totalGrossSalary = totalGrossSalary.toString(),
    totalDeductions = totalDeductions.toString(),
    totalNetSalary = totalNetSalary.toString(),
    notes = notes,
    createdAt = createdAt?.toString()
)
