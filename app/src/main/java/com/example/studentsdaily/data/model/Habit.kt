package com.example.studentsdaily.data.model

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDays: Int,
    val streak: Int = 0,
    val isCompletedToday: Boolean = false
)
