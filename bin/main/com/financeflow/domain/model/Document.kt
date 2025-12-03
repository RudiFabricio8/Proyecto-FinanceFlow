package com.financeflow.domain.model

import com.financeflow.domain.valueobject.DocumentType
import com.financeflow.domain.valueobject.UploadStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Document(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val payrollPeriodId: UUID? = null,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val fileType: String,
    val documentType: DocumentType? = null,
    val uploadStatus: UploadStatus = UploadStatus.UPLOADED,
    val parsingError: String? = null,
    val extractedAmount: BigDecimal? = null,
    val extractedDate: LocalDate? = null,
    val uploadedBy: UUID,
    val uploadedAt: Instant? = null
)
