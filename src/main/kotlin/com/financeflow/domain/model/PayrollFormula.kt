package com.financeflow.domain.model

import java.time.Instant
import java.util.UUID

data class PayrollFormula(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val name: String,
    val description: String? = null,
    val formulaExpression: String,
    val variables: Map<String, String>? = null,
    val createdBy: UUID,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
