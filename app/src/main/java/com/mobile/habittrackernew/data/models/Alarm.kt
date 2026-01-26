package com.mobile.habittrackernew.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val repeatDays: String = "", // Comma-separated: "0,1,2,3,4,5,6" for Sun-Sat
    val soundUri: String? = null,
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),

    // Snooze tracking fields
    val snoozeCount: Int = 0,
    val snoozedTime: Long? = null, // Timestamp when snoozed alarm will ring
    val isSnoozed: Boolean = false
) {
    fun getFormattedTime(): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }

    fun getFormattedSnoozedTime(): String? {
        if (snoozedTime == null) return null

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = snoozedTime
        }
        val snoozedHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val snoozedMinute = calendar.get(java.util.Calendar.MINUTE)

        val amPm = if (snoozedHour >= 12) "PM" else "AM"
        val displayHour = when {
            snoozedHour == 0 -> 12
            snoozedHour > 12 -> snoozedHour - 12
            else -> snoozedHour
        }
        return String.format("%d:%02d %s", displayHour, snoozedMinute, amPm)
    }

    fun getRepeatDaysList(): List<Int> {
        return if (repeatDays.isEmpty()) {
            emptyList()
        } else {
            repeatDays.split(",").mapNotNull { it.toIntOrNull() }
        }
    }

    fun getRepeatDaysText(): String {
        val days = getRepeatDaysList()
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        if (days == listOf(1, 2, 3, 4, 5)) return "Weekdays"
        if (days == listOf(0, 6)) return "Weekends"

        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return days.map { dayNames[it] }.joinToString(", ")
    }
}