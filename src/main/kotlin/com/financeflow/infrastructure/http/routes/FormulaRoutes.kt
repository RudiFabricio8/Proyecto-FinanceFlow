package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.model.PayrollFormula
import com.financeflow.domain.model.PayrollCalculation
import com.financeflow.domain.ports.PayrollFormulaRepository
import com.financeflow.domain.ports.PayrollCalculationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Serializable
data class CreateFormulaRequest(
    val organizationId: String,
    val name: String,
    val description: String? = null,
    val formulaExpression: String,
    val variables: Map<String, String>? = null
)

@Serializable
data class FormulaResponse(
    val id: String,
    val name: String,
    val description: String?,
    val formulaExpression: String,
    val variables: Map<String, String>?,
    val createdAt: String?
)

@Serializable
data class ExecuteCalculationRequest(
    val formulaId: String? = null,
    val payrollPeriodId: String,
    val concept: String,
    val inputValues: Map<String, Double>
)

@Serializable
data class CalculationResponse(
    val id: String,
    val concept: String,
    val resultAmount: String,
    val calculationSteps: List<String>?,
    val calculatedAt: String?
)

fun Route.formulaRoutes() {
    val formulaRepository by inject<PayrollFormulaRepository>()
    val calculationRepository by inject<PayrollCalculationRepository>()

    route("/payroll-formulas") {
        authenticate("auth-jwt") {
            post {
                val request = call.receive<CreateFormulaRequest>()
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                
                val formula = PayrollFormula(
                    organizationId = UUID.fromString(request.organizationId),
                    name = request.name,
                    description = request.description,
                    formulaExpression = request.formulaExpression,
                    variables = request.variables,
                    createdBy = UUID.fromString(userId),
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                val created = formulaRepository.create(formula)
                call.respond(HttpStatusCode.Created, created.toResponse())
            }

            get {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val formulas = formulaRepository.findByOrganizationId(UUID.fromString(organizationId))
                call.respond(formulas.map { it.toResponse() })
            }

            delete("/{id}") {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                formulaRepository.delete(UUID.fromString(id))
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

    route("/payroll-calculations") {
        authenticate("auth-jwt") {
            post {
                val request = call.receive<ExecuteCalculationRequest>()
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                
                // Simple calculation logic (can be enhanced)
                val result = request.inputValues.values.sum()
                val steps = request.inputValues.map { (key, value) -> "$key: $value" }
                
                val calculation = PayrollCalculation(
                    formulaId = request.formulaId?.let { UUID.fromString(it) },
                    payrollPeriodId = UUID.fromString(request.payrollPeriodId),
                    concept = request.concept,
                    inputValues = request.inputValues,
                    resultAmount = BigDecimal.valueOf(result),
                    calculationSteps = steps + "Total: $result",
                    calculatedBy = UUID.fromString(userId),
                    calculatedAt = Instant.now()
                )
                
                val created = calculationRepository.create(calculation)
                call.respond(HttpStatusCode.Created, created.toResponse())
            }

            get {
                val periodId = call.request.queryParameters["periodId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "periodId required"))
                
                val calculations = calculationRepository.findByPeriodId(UUID.fromString(periodId))
                call.respond(calculations.map { it.toResponse() })
            }
        }
    }
}

private fun PayrollFormula.toResponse() = FormulaResponse(
    id = id.toString(),
    name = name,
    description = description,
    formulaExpression = formulaExpression,
    variables = variables,
    createdAt = createdAt?.toString()
)

private fun PayrollCalculation.toResponse() = CalculationResponse(
    id = id.toString(),
    concept = concept,
    resultAmount = resultAmount.toString(),
    calculationSteps = calculationSteps,
    calculatedAt = calculatedAt?.toString()
)
