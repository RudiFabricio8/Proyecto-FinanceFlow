package com.example.payroll.services

import com.example.payroll.database.EmployeesTable
import com.example.payroll.models.Employee
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object EmployeeService {

    // Obtener todos los empleados
    fun getAll(): List<Employee> = transaction {
        EmployeesTable.selectAll().map {
            Employee(
                id = it[EmployeesTable.id],
                userId = it[EmployeesTable.userId],
                nss = it[EmployeesTable.nss],
                position = it[EmployeesTable.position],
                baseSalary = it[EmployeesTable.baseSalary],
                paymentFrequency = it[EmployeesTable.paymentFrequency]
            )
        }
    }

    // Insertar un nuevo empleado
    fun insert(employee: Employee): Employee = transaction {
        val id = EmployeesTable.insertAndGetId {
            it[userId] = employee.userId
            it[nss] = employee.nss
            it[position] = employee.position
            it[baseSalary] = employee.baseSalary
            it[paymentFrequency] = employee.paymentFrequency
        }.value

        employee.copy(id = id)
    }
}
