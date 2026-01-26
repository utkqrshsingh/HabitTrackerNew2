package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "FitnessCenter", // Store icon name as string
    val colorHex: String = "#4CAF50", // Store color as hex string
    val targetMinutes: Int = 30,
    val reminderTime: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0 // For custom ordering
)

// Default habits to seed on first launch
object DefaultHabits {
    val habits = listOf(
        Habit(
            name = "Yoga",
            description = "Daily yoga practice",
            iconName = "SelfImprovement",
            colorHex = "#9C27B0",
            orderIndex = 0
        ),
        Habit(
            name = "Study",
            description = "Learning activities",
            iconName = "MenuBook",
            colorHex = "#2196F3",
            orderIndex = 1
        ),
        Habit(
            name = "Diet",
            description = "Healthy eating",
            iconName = "Restaurant",
            colorHex = "#4CAF50",
            orderIndex = 2
        ),
        Habit(
            name = "Exercise",
            description = "Physical workout",
            iconName = "FitnessCenter",
            colorHex = "#FF5722",
            orderIndex = 3
        ),
        Habit(
            name = "Meditation",
            description = "Mindfulness practice",
            iconName = "Spa",
            colorHex = "#00BCD4",
            orderIndex = 4
        ),
        Habit(
            name = "Hydration",
            description = "Stay hydrated",
            iconName = "WaterDrop",
            colorHex = "#03A9F4",
            orderIndex = 5
        ),
        Habit(
            name = "Sleep",
            description = "Quality rest",
            iconName = "Bedtime",
            colorHex = "#673AB7",
            orderIndex = 6
        ),
        Habit(
            name = "Self Care",
            description = "Personal wellness",
            iconName = "Favorite",
            colorHex = "#E91E63",
            orderIndex = 7
        )
    )
}