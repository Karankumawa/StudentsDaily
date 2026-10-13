package com.example.studentsdaily.data.model

import java.util.UUID

enum class Priority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val dueDate: String,
    val priority: Priority,
    val isCompleted: Boolean = false
)
