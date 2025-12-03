package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.ports.PayrollCalculationRepository
import com.financeflow.domain.valueobject.PeriodStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class AnalyticsOverviewResponse(
    val totalCost: String,
    val totalGrossSalary: String,
    val totalDeductions: String,
    val periodBreakdown: List<PeriodBreakdown>,
    val statusSummary: StatusSummary
)

@Serializable
data class PeriodBreakdown(
    val period: String,
    val grossSalary: String,
    val deductions: String,
    val netSalary: String,
    val status: String
)

@Serializable
data class StatusSummary(
    val draft: Int,
    val processed: Int,
    val paused: Int
)

@Serializable
data class TimeseriesResponse(
    val series: List<TimeseriesDataPoint>
)

@Serializable
data class TimeseriesDataPoint(
    val date: String,
    val grossSalary: String,
    val deductions: String,
    val netSalary: String,
    val status: String
)

fun Route.analyticsRoutes() {
    val periodRepository by inject<PayrollPeriodRepository>()
    val calculationRepository by inject<PayrollCalculationRepository>()

    route("/analytics") {
        authenticate("auth-jwt") {
            get("/overview") {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val orgId = UUID.fromString(organizationId)
                val periods = periodRepository.findByOrganizationId(orgId)
                
                val totalGross = periods.sumOf { it.totalGrossSalary }
                val totalDeductions = periods.sumOf { it.totalDeductions }
                val totalNet = periods.sumOf { it.totalNetSalary }
                val totalCost = totalGross + totalDeductions
                
                val breakdown = periods.map { period ->
                    PeriodBreakdown(
                        period = period.name,
                        grossSalary = period.totalGrossSalary.toString(),
                        deductions = period.totalDeductions.toString(),
                        netSalary = period.totalNetSalary.toString(),
                        status = period.status.name
                    )
                }
                
                val statusSummary = StatusSummary(
                    draft = periods.count { it.status == PeriodStatus.DRAFT },
                    processed = periods.count { it.status == PeriodStatus.PROCESSED },
                    paused = periods.count { it.status == PeriodStatus.PAUSED }
                )
                
                call.respond(AnalyticsOverviewResponse(
                    totalCost = totalCost.toString(),
                    totalGrossSalary = totalGross.toString(),
                    totalDeductions = totalDeductions.toString(),
                    periodBreakdown = breakdown,
                    statusSummary = statusSummary
                ))
            }

            get("/timeseries") {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val orgId = UUID.fromString(organizationId)
                val periods = periodRepository.findByOrganizationId(orgId)
                
                val series = periods.map { period ->
                    TimeseriesDataPoint(
                        date = period.startDate.toString(),
                        grossSalary = period.totalGrossSalary.toString(),
                        deductions = period.totalDeductions.toString(),
                        netSalary = period.totalNetSalary.toString(),
                        status = period.status.name
                    )
                }
                
                call.respond(TimeseriesResponse(series = series))
            }
        }
    }
}
