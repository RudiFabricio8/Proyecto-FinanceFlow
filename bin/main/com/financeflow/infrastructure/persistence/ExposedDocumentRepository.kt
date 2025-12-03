package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.Document
import com.financeflow.domain.ports.DocumentRepository
import com.financeflow.domain.valueobject.DocumentType
import com.financeflow.domain.valueobject.UploadStatus
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedDocumentRepository : DocumentRepository {
    override suspend fun create(document: Document): Document = dbQuery {
        Documents.insert {
            it[id] = document.id
            it[organizationId] = document.organizationId
            it[payrollPeriodId] = document.payrollPeriodId
            it[fileName] = document.fileName
            it[filePath] = document.filePath
            it[fileSizeBytes] = document.fileSizeBytes
            it[fileType] = document.fileType
            it[documentType] = document.documentType?.name
            it[uploadStatus] = document.uploadStatus.name
            it[parsingError] = document.parsingError
            it[extractedAmount] = document.extractedAmount
            it[extractedDate] = document.extractedDate
            it[uploadedBy] = document.uploadedBy
            it[uploadedAt] = document.uploadedAt
        }
        document
    }

    override suspend fun findById(id: UUID): Document? = dbQuery {
        Documents.select { Documents.id eq id }
            .map(::resultRowToDocument)
            .singleOrNull()
    }

    override suspend fun findByOrganizationId(organizationId: UUID, page: Int, size: Int): List<Document> = dbQuery {
        Documents.select { Documents.organizationId eq organizationId }
            .orderBy(Documents.uploadedAt to SortOrder.DESC)
            .limit(size, offset = (page * size).toLong())
            .map(::resultRowToDocument)
    }

    override suspend fun findByPeriodId(periodId: UUID): List<Document> = dbQuery {
        Documents.select { Documents.payrollPeriodId eq periodId }
            .orderBy(Documents.uploadedAt to SortOrder.DESC)
            .map(::resultRowToDocument)
    }

    override suspend fun update(document: Document): Document = dbQuery {
        Documents.update({ Documents.id eq document.id }) {
            it[uploadStatus] = document.uploadStatus.name
            it[parsingError] = document.parsingError
            it[extractedAmount] = document.extractedAmount
            it[extractedDate] = document.extractedDate
        }
        document
    }

    override suspend fun delete(id: UUID): Unit = dbQuery {
        Documents.deleteWhere { Documents.id eq id }
    }

    override suspend fun countByOrganizationId(organizationId: UUID): Long = dbQuery {
        Documents.select { Documents.organizationId eq organizationId }.count()
    }

    private fun resultRowToDocument(row: ResultRow) = Document(
        id = row[Documents.id],
        organizationId = row[Documents.organizationId],
        payrollPeriodId = row[Documents.payrollPeriodId],
        fileName = row[Documents.fileName],
        filePath = row[Documents.filePath],
        fileSizeBytes = row[Documents.fileSizeBytes],
        fileType = row[Documents.fileType],
        documentType = row[Documents.documentType]?.let { DocumentType.valueOf(it) },
        uploadStatus = UploadStatus.valueOf(row[Documents.uploadStatus]),
        parsingError = row[Documents.parsingError],
        extractedAmount = row[Documents.extractedAmount],
        extractedDate = row[Documents.extractedDate],
        uploadedBy = row[Documents.uploadedBy],
        uploadedAt = row[Documents.uploadedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
