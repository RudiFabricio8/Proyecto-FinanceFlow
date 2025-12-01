package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import com.financeflow.domain.repository.TransactionRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.sum
import org.ktorm.dsl.QueryRowSet
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class TransactionRepositoryImpl(private val database: Database) : TransactionRepository {

    override suspend fun findAll(): List<Transaction> {
        return database.from(Transactions)
            .select()
            .map { it.toTransaction() }
    }

    override suspend fun findById(id: UUID): Transaction? {
        return database.from(Transactions)
            .select()
            .where { Transactions.id eq id }
            .map { it.toTransaction() }
            .firstOrNull()
    }

    override suspend fun save(entity: Transaction): Transaction {
        val now = System.currentTimeMillis()

        val affectedRecords = database.update(Transactions) {
            set(Transactions.userId, entity.userId)
            set(Transactions.amount, entity.amount)
            set(Transactions.type, entity.type.name)
            set(Transactions.category, entity.category)
            set(Transactions.description, entity.description)
            set(Transactions.date, entity.date)
            set(Transactions.reference, entity.reference)
            set(Transactions.status, entity.status.name)
            set(Transactions.updatedAt, now)
            where { Transactions.id eq entity.id }
        }

        return if (affectedRecords == 0) {
            val newTransaction = entity.copy(
                id = UUID.randomUUID(),
                createdAt = now,
                updatedAt = now
            )

            database.insert(Transactions) {
                set(Transactions.id, newTransaction.id)
                set(Transactions.userId, newTransaction.userId)
                set(Transactions.amount, newTransaction.amount)
                set(Transactions.type, newTransaction.type.name)
                set(Transactions.category, newTransaction.category)
                set(Transactions.description, newTransaction.description)
                set(Transactions.date, newTransaction.date)
                set(Transactions.reference, newTransaction.reference)
                set(Transactions.status, newTransaction.status.name)
                set(Transactions.createdAt, newTransaction.createdAt)
                set(Transactions.updatedAt, newTransaction.updatedAt)
            }

            newTransaction
        } else {
            entity.copy(updatedAt = now)
        }
    }

    override suspend fun delete(id: UUID): Boolean {
        val affectedRows = database.delete(Transactions) { Transactions.id eq id }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        return database.from(Transactions)
            .select(Transactions.id)
            .where { Transactions.id eq id }
            .totalRecords > 0
    }

    override suspend fun findByUserId(userId: UUID): List<Transaction> {
        return database.from(Transactions)
            .select()
            .where { Transactions.userId eq userId }
            .orderBy(Transactions.date.desc())
            .map { it.toTransaction() }
    }

    override suspend fun findByUserIdAndType(userId: UUID, type: TransactionType): List<Transaction> {
        return database.from(Transactions)
            .select()
            .where {
                (Transactions.userId eq userId) and
                        (Transactions.type eq type.name)
            }
            .orderBy(Transactions.date.desc())
            .map { it.toTransaction() }
    }

    override suspend fun findByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction> {
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000L
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000L

        return database.from(Transactions)
            .select()
            .where {
                (Transactions.userId eq userId) and
                        (Transactions.date greaterEq startMillis) and
                        (Transactions.date less endMillis)
            }
            .orderBy(Transactions.date.desc())
            .map { it.toTransaction() }
    }

    override suspend fun updateStatus(transactionId: UUID, status: TransactionStatus): Boolean {
        val affectedRows = database.update(Transactions) {
            set(Transactions.status, status.name)
            set(Transactions.updatedAt, System.currentTimeMillis())
            where { Transactions.id eq transactionId }
        }
        return affectedRows > 0
    }

    override suspend fun findByStatus(status: TransactionStatus): List<Transaction> {
        return database.from(Transactions)
            .select()
            .where { Transactions.status eq status.name }
            .orderBy(Transactions.date.desc())
            .map { it.toTransaction() }
    }

    override suspend fun getTotalAmountByUserAndType(userId: UUID, type: TransactionType): Double {
        val amountAlias = sum(Transactions.amount).aliased("total_amount")

        return database.from(Transactions)
            .select(amountAlias)
            .where {
                (Transactions.userId eq userId) and
                        (Transactions.type eq type.name) and
                        (Transactions.status eq TransactionStatus.COMPLETED.name)
            }
            .map { row ->
                val total: Number? = row[amountAlias]
                total?.toDouble() ?: 0.0
            }
            .firstOrNull() ?: 0.0
    }

    private fun QueryRowSet.toTransaction(): Transaction {
        return Transaction(
            id = this[Transactions.id]!!,
            userId = this[Transactions.userId]!!,
            amount = this[Transactions.amount]!!.toDouble(),
            type = TransactionType.valueOf(this[Transactions.type]!!),
            category = this[Transactions.category]!!,
            description = this[Transactions.description],
            date = this[Transactions.date]!!,
            reference = this[Transactions.reference],
            status = TransactionStatus.valueOf(this[Transactions.status]!!),
            createdAt = this[Transactions.createdAt]!!,
            updatedAt = this[Transactions.updatedAt]!!
        )
    }
}
