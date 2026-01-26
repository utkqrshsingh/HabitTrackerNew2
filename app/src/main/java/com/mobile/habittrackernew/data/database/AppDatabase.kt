package com.mobile.habittrackernew.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mobile.habittrackernew.data.models.AIMessage
import com.mobile.habittrackernew.data.models.Habit
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.data.models.UserProfile

@Database(
    entities = [Habit::class, HabitLog::class, UserProfile::class, AIMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aiMessageDao(): AIMessageDao
}