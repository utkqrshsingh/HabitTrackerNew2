package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val name: String,
    val description: String = "",
    val targetMinutes: Int = 30,
    val reminderTime: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)