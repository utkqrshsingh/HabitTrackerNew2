package com.mobile.habittrackernew.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobile.habittrackernew.data.models.*

@Database(
    entities = [
        Habit::class,
        HabitLog::class,
        UserProfile::class,
        AIMessage::class,
        Alarm::class
    ],
    version = 5,  // Increment version
    exportSchema = false
)
@TypeConverters(Converters::class)  // ADD THIS LINE
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}