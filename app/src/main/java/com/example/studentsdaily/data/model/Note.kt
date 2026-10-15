package com.example.studentsdaily.data.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val dateString: String,
    val categoryTag: String,
    val isPinned: Boolean = false
)
