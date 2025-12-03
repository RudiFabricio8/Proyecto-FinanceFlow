package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.valueobject.PeriodStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.UUID

@Serializable
data class ComplianceResponse(
    val year: Int,
    val month: Int,
    val dailyRecords: List<DailyComplianceRecord>,
    val summary: ComplianceSummary
)

@Serializable
data class DailyComplianceRecord(
    val date: String,
    val periodsCount: Int,
    val status: String,
    val totalAmount: String
)

@Serializable
data class ComplianceSummary(
    val totalPeriods: Int,
    val processedCount: Int,
    val draftCount: Int,
    val pausedCount: Int
)

fun Route.adminRoutes() {
    val periodRepository by inject<PayrollPeriodRepository>()

    route("/admin") {
        authenticate("auth-jwt") {
            get("/compliance") {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val year = call.request.queryParameters["year"]?.toIntOrNull() ?: LocalDate.now().year
                val month = call.request.queryParameters["month"]?.toIntOrNull() ?: LocalDate.now().monthValue
                
                val orgId = UUID.fromString(organizationId)
                val allPeriods = periodRepository.findByOrganizationId(orgId)
                
                // Filter periods by year/month
                val filteredPeriods = allPeriods.filter { period ->
                    period.startDate.year == year && period.startDate.monthValue == month
                }
                
                // Group by date
                val dailyRecords = filteredPeriods.groupBy { it.startDate }
                    .map { (date, periods) ->
                        DailyComplianceRecord(
                            date = date.toString(),
                            periodsCount = periods.size,
                            status = periods.firstOrNull()?.status?.name ?: "DRAFT",
                            totalAmount = periods.sumOf { it.totalNetSalary }.toString()
                        )
                    }
                    .sortedBy { it.date }
                
                val summary = ComplianceSummary(
                    totalPeriods = filteredPeriods.size,
                    processedCount = filteredPeriods.count { it.status == PeriodStatus.PROCESSED },
                    draftCount = filteredPeriods.count { it.status == PeriodStatus.DRAFT },
                    pausedCount = filteredPeriods.count { it.status == PeriodStatus.PAUSED }
                )
                
                call.respond(ComplianceResponse(
                    year = year,
                    month = month,
                    dailyRecords = dailyRecords,
                    summary = summary
                ))
            }

            get("/compliance/export") {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                // TODO: Implement CSV/Excel export
                call.respond(HttpStatusCode.NotImplemented, mapOf("message" to "Export feature coming soon"))
            }
        }
    }
}
