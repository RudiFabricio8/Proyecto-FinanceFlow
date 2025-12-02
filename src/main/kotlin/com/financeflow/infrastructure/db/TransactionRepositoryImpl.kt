package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import com.financeflow.domain.repository.TransactionRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.math.BigDecimal

class TransactionRepositoryImpl(private val database: Database) : TransactionRepository {

    private fun UUID.toLongId(): Long = try {
        this.toString().split("-").first().toLong(16)
    } catch (e: Exception) {
        0L
    }

    private fun Long.toUuid(): UUID = UUID.nameUUIDFromBytes(this.toString().toByteArray())

    override suspend fun findByUserId(userId: UUID): List<Transaction> {
        val longUserId = userId.toLongId()
        return database.from(PaymentHistory)
            .select()
            .where { PaymentHistory.employeeId eq longUserId }
            .map { it.toTransaction() }
    }

    override suspend fun findByUserIdAndType(userId: UUID, type: TransactionType): List<Transaction> {
        val longUserId = userId.toLongId()
        return database.from(PaymentHistory)
            .select()
            .where {
                (PaymentHistory.employeeId eq longUserId) and
                (PaymentHistory.paymentStatus eq type.name)
            }
            .map { it.toTransaction() }
    }

    override suspend fun findByUserIdAndDateRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<Transaction> {
        val longUserId = userId.toLongId()
        return database.from(PaymentHistory)
            .select()
            .where {
                (PaymentHistory.employeeId eq longUserId) and
                (PaymentHistory.paymentDate greaterEq startDate) and
                (PaymentHistory.paymentDate lessEq endDate)
            }
            .map { it.toTransaction() }
    }

    override suspend fun updateStatus(transactionId: UUID, status: TransactionStatus): Boolean {
        val longId = transactionId.toLongId()
        val affected = database.update(PaymentHistory) {
            set(it.paymentStatus, status.name)
            where { it.id eq longId }
        }
        return affected > 0
    }

    override suspend fun findByStatus(status: TransactionStatus): List<Transaction> {
        return database.from(PaymentHistory)
            .select()
            .where { PaymentHistory.paymentStatus eq status.name }
            .map { it.toTransaction() }
    }

    override suspend fun getTotalAmountByUserAndType(userId: UUID, type: TransactionType): Double {
        val longUserId = userId.toLongId()
        return database.from(PaymentHistory)
            .select(sum(PaymentHistory.paymentAmount))
            .where {
                (PaymentHistory.employeeId eq longUserId) and
                (PaymentHistory.paymentStatus eq type.name)
            }
            .map { it.getDouble(1) ?: 0.0 }
            .firstOrNull() ?: 0.0
    }

    private fun QueryRowSet.toTransaction(): Transaction {
        val paymentDate = this[PaymentHistory.paymentDate]!!
        val dateTimestamp = paymentDate.toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        return Transaction(
            id = this[PaymentHistory.id]!!.toUuid(),
            userId = this[PaymentHistory.employeeId]!!.toUuid(),
            amount = this[PaymentHistory.paymentAmount]!!.toDouble(),
            type = TransactionType.INCOME, // Por defecto, ya que payment_history es para pagos
            category = "PAYROLL",
            description = "Payment from payroll",
            date = dateTimestamp,
            reference = null,
            status = TransactionStatus.valueOf(this[PaymentHistory.paymentStatus]!!),
            createdAt = this[PaymentHistory.createdAt]!!
        )
    }
}
