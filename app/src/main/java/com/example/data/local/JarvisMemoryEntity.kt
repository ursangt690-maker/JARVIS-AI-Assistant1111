package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_memories")
data class JarvisMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val memoryKey: String,
    val memoryValue: String,
    val category: String = "general",
    val timestamp: Long = System.currentTimeMillis()
)
