package com.example.payroll.services

import com.example.payroll.database.EmployeeDocumentsTable
import com.example.payroll.models.EmployeeDocument
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object EmployeeDocumentService {

    // ✅ Obtener todos los documentos
    fun getAll(): List<EmployeeDocument> = transaction {
        EmployeeDocumentsTable.selectAll().map {
            EmployeeDocument(
                id = it[EmployeeDocumentsTable.id],
                employeeId = it[EmployeeDocumentsTable.employeeId],
                documentTypeId = it[EmployeeDocumentsTable.documentTypeId],
                documentUrl = it[EmployeeDocumentsTable.documentUrl]
            )
        }
    }

    // ✅ Insertar un nuevo documento
    fun insert(document: EmployeeDocument): EmployeeDocument = transaction {
        val id = EmployeeDocumentsTable.insertAndGetId {
            it[employeeId] = document.employeeId
            it[documentTypeId] = document.documentTypeId
            it[documentUrl] = document.documentUrl
        }.value

        document.copy(id = id)
    }
}
