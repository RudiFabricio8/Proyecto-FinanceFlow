package com.financeflow.infrastructure.db

import com.financeflow.domain.model.User
import com.financeflow.domain.repository.UserRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.QueryRowSet
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepositoryImpl(private val database: Database) : UserRepository {

    // Helper para conversión de UUID (del modelo) a Long (de la BD)
    private fun UUID.toLongId(): Long = try {
        this.toString().split("-").first().toLong(16)
    } catch (e: Exception) {
        0L
    }

    // Helper para simular un UUID a partir de un Long de la BD
    private fun Long.toUuid(): UUID = UUID.nameUUIDFromBytes(this.toString().toByteArray())

    override suspend fun findAll(): List<User> {
        return database.from(Users)
            .select()
            .map { it.toUser() }
    }

    override suspend fun findById(id: UUID): User? {
        val longId = id.toLongId()
        return database.from(Users)
            .select()
            .where { Users.id eq longId }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun findByEmail(email: String): User? {
        return database.from(Users)
            .select()
            .where { Users.email eq email.lowercase() }
            .map { it.toUser() }
            .firstOrNull()
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return database.from(Users)
            .select(Users.id)
            .where { Users.email eq email.lowercase() }
            .totalRecords > 0
    }

    override suspend fun save(entity: User): User {
        val now = System.currentTimeMillis()
        val encryptedPassword = BCrypt.hashpw(entity.password, BCrypt.gensalt())
        val longId = entity.id.toLongId()

        // 1. Intentar actualizar
        val affectedRecords = database.update(Users) {
            set(it.fullName, entity.fullName)
            set(it.email, entity.email.lowercase())
            set(it.passwordHash, encryptedPassword)
            set(it.role, entity.role.name)
            set(it.isActive, entity.isActive)
            where { Users.id eq longId }
        }

        if (affectedRecords > 0) {
            return findById(entity.id)!!
        }

        // 2. Insertar nuevo usuario

        val newLongId = database.insertAndGenerateKey(Users) {
            set(it.fullName, entity.fullName)
            set(it.email, entity.email.lowercase())
            set(it.passwordHash, encryptedPassword)
            set(it.role, entity.role.name)
            set(it.isActive, entity.isActive)
            // 🚨 Corrección de tipo: asignando Long a la columna 'long'
            set(it.createdAt, now)
        } as Long

        val newUuid = newLongId.toUuid()

        return findById(newUuid)!!
    }

    override suspend fun delete(id: UUID): Boolean {
        val longId = id.toLongId()
        val affectedRows = database.delete(Users) { Users.id eq longId }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        val longId = id.toLongId()
        return database.from(Users)
            .select(Users.id)
            .where { Users.id eq longId }
            .totalRecords > 0
    }

    override suspend fun updatePassword(userId: UUID, newPassword: String): Boolean {
        val longId = userId.toLongId()
        val encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        val affectedRows = database.update(Users) {
            set(it.passwordHash, encryptedPassword)
            where { it.id eq longId }
        }
        return affectedRows > 0
    }

    override suspend fun deactivate(userId: UUID): Boolean {
        val longId = userId.toLongId()
        val affectedRows = database.update(Users) {
            set(it.isActive, false)
            where { it.id eq longId }
        }
        return affectedRows > 0
    }

    private fun QueryRowSet.toUser(): User {
        val longId = this[Users.id]!!

        return User(
            id = longId.toUuid(),
            fullName = this[Users.fullName]!!,
            email = this[Users.email]!!,
            password = this[Users.passwordHash]!!,
            // 🚨 SOLUCIÓN FINAL: Usar el enum anidado dentro de la clase User
            role = User.Role.valueOf(this[Users.role]!!),
            isActive = this[Users.isActive]!!,
            createdAt = this[Users.createdAt]!!
        )
    }
}