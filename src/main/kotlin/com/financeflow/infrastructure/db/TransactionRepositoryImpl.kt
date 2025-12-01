package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import com.financeflow.domain.repository.TransactionRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

object Transactions : Table<Nothing>("transactions") {
    val id = uuid("id").primaryKey()
    val userId = uuid("user_id")
    val amount = double("amount")
    val type = varchar("type")
    val category = varchar("category")
    val description = varchar("description")
    val date = long("date")          // millis desde epoch
    val reference = varchar("reference")
    val status = varchar("status")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
}

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
            applyCommonFields(entity)
            set(it.updatedAt, now)
            where { it.id eq entity.id }
        }

        return if (affectedRecords == 0) {
            val newTransaction = entity.copy(
                id = UUID.randomUUID(),
                createdAt = now,
                updatedAt = now
            )

            database.insert(Transactions) {
                set(it.id, newTransaction.id)
                applyCommonFields(newTransaction)
                set(it.createdAt, newTransaction.createdAt)
                set(it.updatedAt, newTransaction.updatedAt)
            }

            newTransaction
        } else {
            entity.copy(updatedAt = now)
        }
    }

    override suspend fun delete(id: UUID): Boolean {
        val affectedRows = database.delete(Transactions) { it.id eq id }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        val count = database.from(Transactions)
            .select()
            .where { Transactions.id eq id }
            .totalRecordsInAllPages

        return count > 0
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
            set(it.status, status.name)
            set(it.updatedAt, System.currentTimeMillis())
            where { it.id eq transactionId }
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
            .map { it[amountAlias]?.toDouble() ?: 0.0 }
            .firstOrNull() ?: 0.0
    }

    private fun AssignmentsBuilder.applyCommonFields(tx: Transaction) {
        set(Transactions.userId, tx.userId)
        set(Transactions.amount, tx.amount)
        set(Transactions.type, tx.type.name)
        set(Transactions.category, tx.category)
        set(Transactions.description, tx.description)
        set(Transactions.date, tx.date)
        set(Transactions.reference, tx.reference)
        set(Transactions.status, tx.status.name)
    }

    private fun QueryRowSet.toTransaction(): Transaction {
        return Transaction(
            id = this[Transactions.id]!!,
            userId = this[Transactions.userId]!!,
            amount = this[Transactions.amount]!!,
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
