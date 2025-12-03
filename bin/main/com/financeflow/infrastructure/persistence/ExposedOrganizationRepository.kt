package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.Organization
import com.financeflow.domain.ports.OrganizationRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedOrganizationRepository : OrganizationRepository {
    override suspend fun create(organization: Organization): Organization = dbQuery {
        Organizations.insert {
            it[id] = organization.id
            it[name] = organization.name
            it[taxId] = organization.taxId
            it[ownerId] = organization.ownerId
            it[address] = organization.address
            it[phone] = organization.phone
            it[createdAt] = organization.createdAt
            it[updatedAt] = organization.updatedAt
        }
        organization
    }

    override suspend fun findById(id: UUID): Organization? = dbQuery {
        Organizations.select { Organizations.id eq id }
            .map(::resultRowToOrganization)
            .singleOrNull()
    }

    override suspend fun findByOwnerId(ownerId: UUID): List<Organization> = dbQuery {
        Organizations.select { Organizations.ownerId eq ownerId }
            .map(::resultRowToOrganization)
    }

    override suspend fun update(organization: Organization): Organization = dbQuery {
        Organizations.update({ Organizations.id eq organization.id }) {
            it[name] = organization.name
            it[taxId] = organization.taxId
            it[address] = organization.address
            it[phone] = organization.phone
            it[updatedAt] = organization.updatedAt
        }
        organization
    }

    private fun resultRowToOrganization(row: ResultRow) = Organization(
        id = row[Organizations.id],
        name = row[Organizations.name],
        taxId = row[Organizations.taxId],
        ownerId = row[Organizations.ownerId],
        address = row[Organizations.address],
        phone = row[Organizations.phone],
        createdAt = row[Organizations.createdAt],
        updatedAt = row[Organizations.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
