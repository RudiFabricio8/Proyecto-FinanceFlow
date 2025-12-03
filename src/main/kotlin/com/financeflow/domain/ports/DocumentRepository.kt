package com.financeflow.domain.ports

import com.financeflow.domain.model.Document
import java.util.UUID

interface DocumentRepository {
    suspend fun create(document: Document): Document
    suspend fun findById(id: UUID): Document?
    suspend fun findByOrganizationId(organizationId: UUID, page: Int = 0, size: Int = 20): List<Document>
    suspend fun findByPeriodId(periodId: UUID): List<Document>
    suspend fun update(document: Document): Document
    suspend fun delete(id: UUID)
    suspend fun countByOrganizationId(organizationId: UUID): Long
}
