package com.financeflow.application.services

import com.financeflow.application.dto.*
import com.financeflow.domain.model.*
import com.financeflow.domain.repository.PayrollRepository
import com.financeflow.domain.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class PayrollService(
    private val payrollRepository: PayrollRepository,
    private val userRepository: UserRepository
) : KoinComponent {
    
    private val transactionService: TransactionService by inject()
    
    suspend fun createPayroll(request: PayrollCreateRequest, requestedBy: UUID): PayrollResponse {
        // Verify requester is admin or the same user
        val requester = userRepository.findById(requestedBy) ?: throw NoSuchElementException("User not found")
        val isAdmin = requester.role == UserRole.ADMIN || requester.role == UserRole.ACCOUNTANT
        
        // If not admin, can only create payroll for self
        if (!isAdmin && requestedBy != UUID.fromString(request.userId)) {
            throw SecurityException("Not authorized to create payroll for this user")
        }
        
        val userId = UUID.fromString(request.userId)
        val user = userRepository.findById(userId) ?: throw NoSuchElementException("User not found")
        
        // Calculate net pay
        val totalDeductions = request.deductions.sumOf { it.amount }
        val totalBonuses = request.bonuses.sumOf { it.amount }
        val netPay = request.baseSalary + totalBonuses - totalDeductions
        
        // Create payroll
        val payroll = Payroll(
            userId = userId,
            periodStart = request.periodStart,
            periodEnd = request.periodEnd,
            baseSalary = request.baseSalary,
            deductions = request.deductions.map { it.toDomain() },
            bonuses = request.bonuses.map { it.toDomain() },
            netPay = netPay,
            status = PayrollStatus.DRAFT
        )
        
        val savedPayroll = payrollRepository.save(payroll)
        return savedPayroll.toResponse()
    }
    
    suspend fun getPayrollById(payrollId: UUID, requestedBy: UUID): PayrollResponse {
        val payroll = payrollRepository.findById(payrollId)
            ?: throw NoSuchElementException("Payroll not found with ID: $payrollId")
        
        // Verify requester is admin, accountant, or the payroll owner
        val requester = userRepository.findById(requestedBy) ?: throw NoSuchElementException("User not found")
        val isAdminOrAccountant = requester.role == UserRole.ADMIN || requester.role == UserRole.ACCOUNTANT
        
        if (!isAdminOrAccountant && payroll.userId != requestedBy) {
            throw SecurityException("Not authorized to view this payroll")
        }
        
        return payroll.toResponse()
    }
    
    suspend fun updatePayroll(
        payrollId: UUID,
        request: PayrollUpdateRequest,
        requestedBy: UUID
    ): PayrollResponse {
        val existing = payrollRepository.findById(payrollId)
            ?: throw NoSuchElementException("Payroll not found with ID: $payrollId")
        
        // Verify requester is admin or accountant
        val requester = userRepository.findById(requestedBy) ?: throw NoSuchElementException("User not found")
        val isAdminOrAccountant = requester.role == UserRole.ADMIN || requester.role == UserRole.ACCOUNTANT
        
        if (!isAdminOrAccountant) {
            throw SecurityException("Not authorized to update payrolls")
        }
        
        // Update fields if provided
        val updatedDeductions = request.deductions?.map { it.toDomain() } ?: existing.deductions
        val updatedBonuses = request.bonuses?.map { it.toDomain() } ?: existing.bonuses
        
        // Recalculate net pay if needed
        val totalDeductions = updatedDeductions.sumOf { it.amount }
        val totalBonuses = updatedBonuses.sumOf { it.amount }
        val netPay = existing.baseSalary + totalBonuses - totalDeductions
        
        val updated = existing.copy(
            status = request.status?.let { PayrollStatus.valueOf(it.uppercase()) } ?: existing.status,
            paymentDate = request.paymentDate ?: existing.paymentDate,
            deductions = updatedDeductions,
            bonuses = updatedBonuses,
            netPay = netPay
        )
        
        val saved = payrollRepository.save(updated)
        
        // If status changed to PAID, create a transaction
        if (saved.status == PayrollStatus.PAID && existing.status != PayrollStatus.PAID) {
            createPayrollTransaction(saved, "Payroll payment for period ${formatDate(saved.periodStart)} to ${formatDate(saved.periodEnd)}")
        }
        
        return saved.toResponse()
    }
    
    suspend fun deletePayroll(payrollId: UUID, requestedBy: UUID) {
        val existing = payrollRepository.findById(payrollId)
            ?: throw NoSuchElementException("Payroll not found with ID: $payrollId")
        
        // Verify requester is admin or accountant
        val requester = userRepository.findById(requestedBy) ?: throw NoSuchElementException("User not found")
        val isAdminOrAccountant = requester.role == UserRole.ADMIN || requester.role == UserRole.ACCOUNTANT
        
        if (!isAdminOrAccountant) {
            throw SecurityException("Not authorized to delete payrolls")
        }
        
        payrollRepository.delete(payrollId)
    }
    
    suspend fun getUserPayrolls(
        userId: UUID,
        requestedBy: UUID,
        page: Int = 1,
        pageSize: Int = 20,
        status: String? = null,
        year: Int? = null,
        month: Int? = null
    ): PagedResponse<PayrollResponse> {
        // Verify requester is admin, accountant, or the user themselves
        val requester = userRepository.findById(requestedBy) ?: throw NoSuchElementException("User not found")
        val isAdminOrAccountant = requester.role == UserRole.ADMIN || requester.role == UserRole.ACCOUNTANT
        
        if (!isAdminOrAccountant && requestedBy != userId) {
            throw SecurityException("Not authorized to view these payrolls")
        }
        
        // Build filters
        val allPayrolls = payrollRepository.findByUserId(userId)
            .filter { payroll ->
                status?.let { payroll.status == PayrollStatus.valueOf(it.uppercase()) } ?: true
            }
            .filter { payroll ->
                if (year != null) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = payroll.periodStart }
                    calendar.get(Calendar.YEAR) == year
                } else true
            }
            .filter { payroll ->
                if (month != null) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = payroll.periodStart }
                    calendar.get(Calendar.MONTH) + 1 == month // Calendar.MONTH is 0-based
                } else true
            }
            .sortedByDescending { it.periodEnd }
        
        // Apply pagination
        val totalItems = allPayrolls.size
        val totalPages = (totalItems + pageSize - 1) / pageSize
        val paginated = allPayrolls
            .drop((page - 1) * pageSize)
            .take(pageSize)
            .map { it.toResponse() }
        
        return PagedResponse(
            data = paginated,
            page = page,
            pageSize = pageSize,
            totalItems = totalItems.toLong(),
            totalPages = totalPages
        )
    }
    
    suspend fun getPayrollSummary(
        userId: UUID? = null,
        year: Int? = null,
        month: Int? = null
    ): PayrollSummary {
        // Get all payrolls, filtered by user if specified
        val payrolls = (userId?.let { payrollRepository.findByUserId(it) } 
            ?: payrollRepository.findAll())
            .filter { payroll ->
                if (year != null) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = payroll.periodStart }
                    calendar.get(Calendar.YEAR) == year
                } else true
            }
            .filter { payroll ->
                if (month != null) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = payroll.periodStart }
                    calendar.get(Calendar.MONTH) + 1 == month // Calendar.MONTH is 0-based
                } else true
            }
        
        val totalPayrolls = payrolls.size
        val totalAmount = payrolls.sumOf { it.netPay }
        val averageNetPay = if (payrolls.isNotEmpty()) totalAmount / payrolls.size else 0.0
        
        val statusBreakdown = payrolls
            .groupBy { it.status }
            .mapValues { (_, payrolls) -> payrolls.size }
            .mapKeys { it.key.name }
        
        return PayrollSummary(
            totalPayrolls = totalPayrolls,
            totalAmount = totalAmount,
            averageNetPay = averageNetPay,
            statusBreakdown = statusBreakdown
        )
    }
    
    private suspend fun createPayrollTransaction(payroll: Payroll, description: String) {
        val transactionRequest = TransactionCreateRequest(
            amount = payroll.netPay,
            type = "INCOME",
            category = "SALARY",
            description = description,
            date = payroll.paymentDate ?: System.currentTimeMillis()
        )
        
        transactionService.createTransaction(payroll.userId, transactionRequest)
    }
    
    private fun formatDate(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()
    }
    
    private fun PayrollDeductionDto.toDomain(): PayrollDeduction {
        return PayrollDeduction(
            name = this.name,
            amount = this.amount,
            type = DeductionType.valueOf(this.type.uppercase()),
            description = this.description
        )
    }
    
    private fun PayrollBonusDto.toDomain(): PayrollBonus {
        return PayrollBonus(
            name = this.name,
            amount = this.amount,
            description = this.description
        )
    }
    
    private fun Payroll.toResponse(): PayrollResponse {
        return PayrollResponse(
            id = this.id.toString(),
            userId = this.userId.toString(),
            periodStart = this.periodStart,
            periodEnd = this.periodEnd,
            baseSalary = this.baseSalary,
            deductions = this.deductions.map { it.toDto() },
            bonuses = this.bonuses.map { it.toDto() },
            netPay = this.netPay,
            status = this.status.name.lowercase(),
            paymentDate = this.paymentDate,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
    
    private fun PayrollDeduction.toDto(): PayrollDeductionDto {
        return PayrollDeductionDto(
            name = this.name,
            amount = this.amount,
            type = this.type.name,
            description = this.description
        )
    }
    
    private fun PayrollBonus.toDto(): PayrollBonusDto {
        return PayrollBonusDto(
            name = this.name,
            amount = this.amount,
            description = this.description
        )
    }
}
