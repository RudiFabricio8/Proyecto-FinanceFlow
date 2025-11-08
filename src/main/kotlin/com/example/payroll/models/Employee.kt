package com.example.payroll.models

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Int? = null,
    val userId: Int,
    val nss: String,
    val position: String,
    val baseSalary: Double,
    val paymentFrequency: String
)
