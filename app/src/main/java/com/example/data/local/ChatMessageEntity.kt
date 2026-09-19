package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val role: String, // "user" or "jarvis"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en",
    val status: String = "completed" // listening, thinking, processing, speaking, completed, error, offline
)
