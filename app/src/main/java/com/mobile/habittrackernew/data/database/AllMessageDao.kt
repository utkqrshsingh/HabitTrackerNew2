package com.mobile.habittrackernew.data.database

import androidx.room.*
import com.mobile.habittrackernew.data.models.AIMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface AIMessageDao {
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AIMessage>>

    @Query("SELECT * FROM ai_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<AIMessage>

    @Insert
    suspend fun insertMessage(message: AIMessage): Long

    @Query("DELETE FROM ai_messages")
    suspend fun clearAllMessages()
}
