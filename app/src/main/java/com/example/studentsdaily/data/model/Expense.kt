package com.example.studentsdaily.data.model

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
