package com.financeflow.infrastructure.persistence

import com.financeflow.domain.model.User
import com.financeflow.domain.ports.UserRepository
import com.financeflow.domain.valueobject.UserRole
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ExposedUserRepository : UserRepository {
    override suspend fun findByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun create(user: User): User = dbQuery {
        Users.insert {
            it[id] = user.id
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[fullName] = user.fullName
            it[role] = user.role.name
            it[isActive] = user.isActive
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
        }
        user
    }

    override suspend fun findById(id: UUID): User? = dbQuery {
        Users.select { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id],
        email = row[Users.email],
        passwordHash = row[Users.passwordHash],
        fullName = row[Users.fullName],
        role = UserRole.valueOf(row[Users.role]),
        isActive = row[Users.isActive],
        createdAt = row[Users.createdAt],
        updatedAt = row[Users.updatedAt]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
