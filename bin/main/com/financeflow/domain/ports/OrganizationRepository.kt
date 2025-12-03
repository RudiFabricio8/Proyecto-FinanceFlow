package com.financeflow.domain.ports

import com.financeflow.domain.model.Organization
import java.util.UUID

interface OrganizationRepository {
    suspend fun create(organization: Organization): Organization
    suspend fun findById(id: UUID): Organization?
    suspend fun findByOwnerId(ownerId: UUID): List<Organization>
    suspend fun update(organization: Organization): Organization
}
