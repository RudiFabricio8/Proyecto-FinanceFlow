package com.financeflow.infrastructure.http.routes

import com.financeflow.domain.model.Organization
import com.financeflow.domain.ports.OrganizationRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.Instant
import java.util.UUID

@Serializable
data class CreateOrganizationRequest(
    val name: String,
    val taxId: String? = null,
    val address: String? = null,
    val phone: String? = null
)

@Serializable
data class UpdateOrganizationRequest(
    val name: String? = null,
    val taxId: String? = null,
    val address: String? = null,
    val phone: String? = null
)

@Serializable
data class OrganizationDetailResponse(
    val id: String,
    val name: String,
    val taxId: String?,
    val address: String?,
    val phone: String?,
    val createdAt: String?
)

fun Route.organizationRoutes() {
    val organizationRepository by inject<OrganizationRepository>()

    route("/organizations") {
        authenticate("auth-jwt") {
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val request = call.receive<CreateOrganizationRequest>()
                
                val organization = Organization(
                    name = request.name,
                    taxId = request.taxId,
                    ownerId = UUID.fromString(userId),
                    address = request.address,
                    phone = request.phone,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                val created = organizationRepository.create(organization)
                call.respond(HttpStatusCode.Created, created.toDetailResponse())
            }

            get {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                val organizations = organizationRepository.findByOwnerId(UUID.fromString(userId))
                call.respond(organizations.map { it.toDetailResponse() })
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val organization = organizationRepository.findById(UUID.fromString(id))
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Organization not found"))
                
                call.respond(organization.toDetailResponse())
            }

            patch("/{id}") {
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "id required"))
                
                val request = call.receive<UpdateOrganizationRequest>()
                val existing = organizationRepository.findById(UUID.fromString(id))
                    ?: return@patch call.respond(HttpStatusCode.NotFound, mapOf("error" to "Organization not found"))
                
                val updated = existing.copy(
                    name = request.name ?: existing.name,
                    taxId = request.taxId ?: existing.taxId,
                    address = request.address ?: existing.address,
                    phone = request.phone ?: existing.phone,
                    updatedAt = Instant.now()
                )
                
                organizationRepository.update(updated)
                call.respond(updated.toDetailResponse())
            }
        }
    }
}

private fun Organization.toDetailResponse() = OrganizationDetailResponse(
    id = id.toString(),
    name = name,
    taxId = taxId,
    address = address,
    phone = phone,
    createdAt = createdAt?.toString()
)
