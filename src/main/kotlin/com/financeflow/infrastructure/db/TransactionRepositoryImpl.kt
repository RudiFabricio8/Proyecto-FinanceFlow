package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Transaction
import com.financeflow.domain.model.TransactionStatus
import com.financeflow.domain.model.TransactionType
import com.financeflow.domain.repository.TransactionRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

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
            set(it.userId, entity.userId)
            set(it.amount, entity.amount)
            set(it.type, entity.type.name)
            set(it.category, entity.category)
            set(it.description, entity.description)
            set(it.date, entity.date)
            set(it.reference, entity.reference)
            set(it.status, entity.status.name)
            set(it.updatedAt, now)
            
            where { it.id eq entity.id }
        }
        
        return if (affectedRecords == 0) {
            // Insert new transaction
            val newTransaction = entity.copy(
                id = UUID.randomUUID(),
                createdAt = now,
                updatedAt = now
            )
            
            database.insert(Transactions) {
                set(it.id, newTransaction.id)
                set(it.userId, newTransaction.userId)
                set(it.amount, newTransaction.amount)
                set(it.type, newTransaction.type.name)
                set(it.category, newTransaction.category)
                set(it.description, newTransaction.description)
                set(it.date, newTransaction.date)
                set(it.reference, newTransaction.reference)
                set(it.status, newTransaction.status.name)
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
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        
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
        return database.from(Transactions)
            .select(Transactions.amount.sum())
            .where { 
                (Transactions.userId eq userId) and 
                (Transactions.type eq type.name) and
                (Transactions.status eq TransactionStatus.COMPLETED.name)
            }
            .map { it.getDouble(1) ?: 0.0 }
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
