package com.example.payroll.models

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeDocument(
    val id: Long = 0,
    val employeeId: Long,
    val documentTypeId: Long,
    val documentUrl: String
)
