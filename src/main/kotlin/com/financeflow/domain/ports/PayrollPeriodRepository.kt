package com.financeflow.domain.ports

import com.financeflow.domain.model.PayrollPeriod
import com.financeflow.domain.valueobject.PeriodStatus
import java.util.UUID

interface PayrollPeriodRepository {
    suspend fun create(period: PayrollPeriod): PayrollPeriod
    suspend fun findById(id: UUID): PayrollPeriod?
    suspend fun findByOrganizationId(organizationId: UUID): List<PayrollPeriod>
    suspend fun findByStatus(organizationId: UUID, status: PeriodStatus): List<PayrollPeriod>
    suspend fun update(period: PayrollPeriod): PayrollPeriod
    suspend fun delete(id: UUID)
}
