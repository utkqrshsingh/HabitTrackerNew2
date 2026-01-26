package com.mobile.habittrackernew.data.models

data class StreakInfo(
    val category: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: String?
)