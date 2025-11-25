package com.financeflow.infrastructure.db

import com.financeflow.domain.model.User
import com.financeflow.domain.model.UserRole
import com.financeflow.domain.repository.UserRepository
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepositoryImpl(private val database: Database) : UserRepository {

    override suspend fun findAll(): List<User> {
        return database.from(Users)
            .select()
            .map { it.toUser() }
    }

    override suspend fun findById(id: UUID): User? {
        return database.from(Users)
            .select()
            .where { Users.id eq id }
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
        val encryptedPassword = BCrypt.hashpw(entity.password, BCrypt.gensalt())
        
        val affectedRecords = database.update(Users) {
            set(it.email, entity.email.lowercase())
            set(it.password, encryptedPassword)
            set(it.fullName, entity.fullName)
            set(it.role, entity.role.name)
            set(it.isActive, entity.isActive)
            set(it.updatedAt, System.currentTimeMillis())
            
            if (entity.id.version() == 0) { // New user
                set(it.id, UUID.randomUUID())
                set(it.createdAt, System.currentTimeMillis())
            } else {
                where { 
                    it.id eq entity.id 
                }
            }
        }
        
        return if (affectedRecords == 0) {
            // Insert new user
            val newUser = entity.copy(
                id = UUID.randomUUID(),
                password = encryptedPassword,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            database.insert(Users) {
                set(it.id, newUser.id)
                set(it.email, newUser.email.lowercase())
                set(it.password, newUser.password)
                set(it.fullName, newUser.fullName)
                set(it.role, newUser.role.name)
                set(it.isActive, newUser.isActive)
                set(it.createdAt, newUser.createdAt)
                set(it.updatedAt, newUser.updatedAt)
            }
            
            newUser
        } else {
            entity.copy(updatedAt = System.currentTimeMillis())
        }
    }

    override suspend fun delete(id: UUID): Boolean {
        val affectedRows = database.delete(Users) { it.id eq id }
        return affectedRows > 0
    }

    override suspend fun existsById(id: UUID): Boolean {
        return database.from(Users)
            .select(Users.id)
            .where { Users.id eq id }
            .totalRecords > 0
    }

    override suspend fun updatePassword(userId: UUID, newPassword: String): Boolean {
        val encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        val affectedRows = database.update(Users) {
            set(it.password, encryptedPassword)
            set(it.updatedAt, System.currentTimeMillis())
            where { it.id eq userId }
        }
        return affectedRows > 0
    }

    override suspend fun deactivate(userId: UUID): Boolean {
        val affectedRows = database.update(Users) {
            set(it.isActive, false)
            set(it.updatedAt, System.currentTimeMillis())
            where { it.id eq userId }
        }
        return affectedRows > 0
    }

    private fun QueryRowSet.toUser(): User {
        return User(
            id = this[Users.id]!!,
            email = this[Users.email]!!,
            password = this[Users.password]!!,
            fullName = this[Users.fullName]!!,
            role = UserRole.valueOf(this[Users.role]!!),
            isActive = this[Users.isActive]!!,
            createdAt = this[Users.createdAt]!!,
            updatedAt = this[Users.updatedAt]!!
        )
    }
}
