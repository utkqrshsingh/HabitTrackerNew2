package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_messages")
data class AIMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)