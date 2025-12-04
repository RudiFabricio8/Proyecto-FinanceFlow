package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.model.Document
import com.financeflow.domain.ports.DocumentRepository
import com.financeflow.domain.valueobject.DocumentType
import com.financeflow.domain.service.NotificationService
import com.financeflow.domain.ports.PayrollPeriodRepository
import com.financeflow.domain.valueobject.UploadStatus
import com.financeflow.domain.valueobject.PeriodStatus
import com.financeflow.domain.model.PayrollPeriod
import java.math.BigDecimal
import java.time.LocalDate
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.io.File
import java.time.Instant
import java.util.UUID
import com.financeflow.domain.ports.NotificationRepository
import com.financeflow.domain.service.DocumentParserService

@Serializable
data class DocumentResponse(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val documentType: String?,
    val uploadStatus: String,
    val extractedAmount: String?,
    val extractedDate: String?,
    val uploadedAt: String?,
    val period: PeriodInfo?
)

@Serializable
data class PeriodInfo(
    val id: String,
    val name: String
)

@Serializable
data class DocumentListResponse(
    val items: List<DocumentResponse>,
    val total: Long,
    val page: Int,
    val size: Int
)

fun Route.documentRoutes() {
    val documentRepository by inject<DocumentRepository>()
    val notificationRepository by inject<NotificationRepository>()
    val notificationService = NotificationService(notificationRepository)
    val documentParserService by inject<DocumentParserService>()
    val payrollPeriodRepository by inject<PayrollPeriodRepository>()

    route("/documents") {
        authenticate("auth-jwt") {
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val multipart = call.receiveMultipart()
                
                var fileName = ""
                var fileBytes: ByteArray? = null
                var organizationId: UUID? = null
                var periodId: UUID? = null
                var documentType: DocumentType? = null
                
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "unknown"
                            fileBytes = part.streamProvider().readBytes()
                        }
                        is PartData.FormItem -> {
                            when (part.name) {
                                "organizationId" -> organizationId = UUID.fromString(part.value)
                                "payrollPeriodId" -> periodId = part.value.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) }
                                "documentType" -> documentType = part.value.takeIf { it.isNotBlank() }?.let { DocumentType.valueOf(it) }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }
                
                if (fileBytes == null || organizationId == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "File and organizationId required"))
                }
                
                val uploadDir = File("uploads")
                if (!uploadDir.exists()) uploadDir.mkdirs()
                
                val fileId = UUID.randomUUID()
                val filePath = "uploads/$fileId-$fileName"
                File(filePath).writeBytes(fileBytes!!)
                
                val document = Document(
                    id = fileId,
                    organizationId = organizationId!!,
                    payrollPeriodId = periodId,
                    fileName = fileName,
                    filePath = filePath,
                    fileSizeBytes = fileBytes!!.size.toLong(),
                    fileType = "application/pdf",
                    documentType = documentType,
                    uploadStatus = UploadStatus.UPLOADED,
                    uploadedBy = UUID.fromString(userId),
                    uploadedAt = Instant.now()
                )
                
                val created = documentRepository.create(document)
                

                try {
                    val pdfFile = File(created.filePath)
                    val metadata = documentParserService.parseDocument(pdfFile, "application/pdf")
                    
                    val period = PayrollPeriod(
                        id = UUID.randomUUID(),
                        organizationId = organizationId!!,
                        name = fileName,
                        startDate = metadata.date ?: java.time.LocalDate.now(),
                        endDate = (metadata.date ?: java.time.LocalDate.now()).plusMonths(1),
                        status = PeriodStatus.DRAFT,
                        totalGrossSalary = metadata.amount?.let { java.math.BigDecimal.valueOf(it) } ?: java.math.BigDecimal.ZERO,
                        totalDeductions = java.math.BigDecimal.ZERO,
                        complianceScore = 0,
                        createdBy = UUID.fromString(userId),
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                    payrollPeriodRepository.create(period)
                    
                    val updatedDocument = created.copy(
                        extractedAmount = metadata.amount?.toBigDecimal(),
                        extractedDate = metadata.date,
                        payrollPeriodId = period.id
                    )
                    documentRepository.update(updatedDocument)
                } catch (e: Exception) {
                    println("Error parsing document: ${e.message}")
                    documentRepository.update(created)
                }
                
                // Return the created or updated document
                val responseDocument = documentRepository.findById(created.id) ?: created

                try {
    kotlinx.coroutines.runBlocking {
        notificationService.createDocumentUploadSuccess(
            userId = UUID.fromString(userId),
            documentId = fileId,
            fileName = fileName
        )
    }
} catch (e: Exception) {
    println("Error creating upload notification: ${e.message}")
}

call.respond(HttpStatusCode.Created, created.toResponse())
            }

            get {
                val organizationId = call.request.queryParameters["organizationId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "organizationId required"))
                
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
                
                val documents = documentRepository.findByOrganizationId(UUID.fromString(organizationId), page, size)
                val total = documentRepository.countByOrganizationId(UUID.fromString(organizationId))
                
                call.respond(DocumentListResponse(
                    items = documents.map { it.toResponse() },
                    total = total,
                    page = page,
                    size = size
                ))
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val document = documentRepository.findById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                
                call.respond(document.toResponse())
            }

            get("/{id}/download") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val document = documentRepository.findById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Document not found"))
                
                val file = File(document.filePath)
                if (!file.exists()) {
                    return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "File not found on disk"))
                }
                
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, document.fileName).toString()
                )
                call.respondFile(file)
            }
        }
    }
}

private fun Document.toResponse() = DocumentResponse(
    id = id.toString(),
    fileName = fileName,
    fileSize = fileSizeBytes,
    documentType = documentType?.name,
    uploadStatus = uploadStatus.name,
    extractedAmount = extractedAmount?.toString(),
    extractedDate = extractedDate?.toString(),
    uploadedAt = uploadedAt?.toString(),
    period = payrollPeriodId?.let { PeriodInfo(it.toString(), "") }
)
