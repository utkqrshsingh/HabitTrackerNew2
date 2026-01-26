package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val category: String,
    val date: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val duration: Int = 0,
    val notes: String = ""
)