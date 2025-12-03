package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.ports.DocumentRepository
import com.financeflow.domain.valueobject.PeriodStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.UUID

@Serializable
data class DashboardSummaryResponse(
    val periodsSummary: PeriodsSummary,
    val totalAmounts: TotalAmounts,
    val recentDocumentsCount: Long
)

@Serializable
data class PeriodsSummary(
    val draft: Int,
    val processed: Int,
    val paused: Int,
    val total: Int
)

@Serializable
data class TotalAmounts(
    val grossSalary: String,
    val deductions: String,
    val netSalary: String
)

fun Route.dashboardRoutes() {
    val periodRepository by inject<PayrollPeriodRepository>()
    val documentRepository by inject<DocumentRepository>()

    route("/dashboard") {
        authenticate("auth-jwt") {
            get("/summary") {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val orgId = UUID.fromString(organizationId)
                val allPeriods = periodRepository.findByOrganizationId(orgId)
                
                val draftCount = allPeriods.count { it.status == PeriodStatus.DRAFT }
                val processedCount = allPeriods.count { it.status == PeriodStatus.PROCESSED }
                val pausedCount = allPeriods.count { it.status == PeriodStatus.PAUSED }
                
                val totalGross = allPeriods.sumOf { it.totalGrossSalary }
                val totalDeductions = allPeriods.sumOf { it.totalDeductions }
                val totalNet = allPeriods.sumOf { it.totalNetSalary }
                
                val documentsCount = documentRepository.countByOrganizationId(orgId)
                
                call.respond(DashboardSummaryResponse(
                    periodsSummary = PeriodsSummary(
                        draft = draftCount,
                        processed = processedCount,
                        paused = pausedCount,
                        total = allPeriods.size
                    ),
                    totalAmounts = TotalAmounts(
                        grossSalary = totalGross.toString(),
                        deductions = totalDeductions.toString(),
                        netSalary = totalNet.toString()
                    ),
                    recentDocumentsCount = documentsCount
                ))
            }
        }
    }
}
