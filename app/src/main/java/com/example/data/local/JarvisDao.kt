package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()

    @Query("SELECT * FROM jarvis_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<JarvisMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: JarvisMemoryEntity): Long

    @Query("DELETE FROM jarvis_memories WHERE id = :memoryId")
    suspend fun deleteMemory(memoryId: Long)

    @Query("DELETE FROM jarvis_memories")
    suspend fun clearMemories()
}
