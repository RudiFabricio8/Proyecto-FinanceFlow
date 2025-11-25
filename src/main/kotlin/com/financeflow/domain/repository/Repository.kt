package com.financeflow.domain.repository

interface Repository<T, ID> {
    suspend fun findAll(): List<T>
    suspend fun findById(id: ID): T?
    suspend fun save(entity: T): T
    suspend fun delete(id: ID): Boolean
    suspend fun existsById(id: ID): Boolean
}
