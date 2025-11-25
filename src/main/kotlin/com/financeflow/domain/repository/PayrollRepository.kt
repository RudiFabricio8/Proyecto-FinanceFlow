package com.financeflow.domain.repository

import com.financeflow.domain.model.Payroll
import com.financeflow.domain.model.PayrollStatus
import java.util.*

interface PayrollRepository : Repository<Payroll, UUID> {
    suspend fun findByUserId(userId: UUID): List<Payroll>
    suspend fun findByUserIdAndStatus(userId: UUID, status: PayrollStatus): List<Payroll>
}
