package com.example.studentsdaily.data.model

import java.util.UUID

enum class TimetableType(val displayName: String) {
    LECTURE("Lecture"),
    LAB("Lab"),
    EXAM("Exam"),
    DEADLINE("Deadline")
}

data class TimetableItem(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val type: TimetableType
)
