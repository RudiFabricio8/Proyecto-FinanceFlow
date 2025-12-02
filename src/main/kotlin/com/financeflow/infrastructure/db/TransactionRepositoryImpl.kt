package com.financeflow.infrastructure.db

import com.financeflow.domain.model.Transaction // 🚨 Nuevo modelo (Asumido)
import com.financeflow.domain.repository.TransactionRepository // 🚨 Nuevo repositorio
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import java.util.UUID

class TransactionRepositoryImpl(private val database: Database) : TransactionRepository {

    // Helper para conversión de UUID (del modelo) a Long (de la BD)
    private fun UUID.toLongId(): Long = try {
        this.toString().split("-").first().toLong(16)
    } catch (e: Exception) {
        0L
    }

    // Helper para simular un UUID a partir de un Long de la BD
    private fun Long.toUuid(): UUID = UUID.nameUUIDFromBytes(this.toString().toByteArray())

    // Usamos PaymentHistory como la tabla de transacciones según tu esquema SQL
    override suspend fun findAll(): List<Transaction> {
        return database.from(PaymentHistory)
            .select()
            .map { it.toTransaction() }
    }

    override suspend fun findById(id: UUID): Transaction? {
        val longId = id.toLongId()
        return database.from(PaymentHistory)
            .select()
            .where { PaymentHistory.id eq longId }
            .map { it.toTransaction() }
            .firstOrNull()
    }

    // save, update, delete y existsById deben ser implementados aquí.
    // Usaremos implementaciones básicas (solo para compilación)

    override suspend fun save(entity: Transaction): Transaction {
        // Implementación básica para compilar
        return entity
    }

    override suspend fun delete(id: UUID): Boolean {
        val longId = id.toLongId()
        val affectedRows = database.delete(PaymentHistory) { PaymentHistory.id eq longId }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        val longId = id.toLongId()
        return database.from(PaymentHistory)
            .select(PaymentHistory.id)
            .where { PaymentHistory.id eq longId }
            .totalRecords > 0
    }

    // Mapeo básico a Transaction (asumiendo que Transaction se parece a PaymentHistory)
    private fun QueryRowSet.toTransaction(): Transaction {
        val longId = this[PaymentHistory.id]!!

        // 🚨 Advertencia: Este mapeo requiere que el modelo Transaction
        // tenga propiedades que coincidan con PaymentHistory.

        return Transaction(
            id = longId.toUuid(),
            // Asumiendo que PaymentHistory.employeeId mapea a un campo userId/employeeId en Transaction
            employeeId = this[PaymentHistory.employeeId]!!.toUuid(),
            amount = this[PaymentHistory.paymentAmount]!!.toDouble(),
            date = this[PaymentHistory.paymentDate]!!.time, // Mapea SQL Date a Long (timestamp)
            status = this[PaymentHistory.paymentStatus]!!,
            createdAt = this[PaymentHistory.createdAt]!!
        )
    }
}