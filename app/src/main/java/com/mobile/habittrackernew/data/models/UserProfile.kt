package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val fitnessGoal: String = "",
    val wakeUpTime: String = "06:00",
    val sleepTime: String = "22:00",
    val dietaryPreference: String = "",
    val createdAt: Long = System.currentTimeMillis()
)