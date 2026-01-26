package com.mobile.habittrackernew.di

import android.content.Context
import androidx.room.Room
import com.mobile.habittrackernew.data.database.*
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "habit_tracker_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideHabitLogDao(database: AppDatabase): HabitLogDao {
        return database.habitLogDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideAIMessageDao(database: AppDatabase): AIMessageDao {
        return database.aiMessageDao()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): NotificationHelper {
        return NotificationHelper(context, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitLogDao: HabitLogDao,
        userProfileDao: UserProfileDao,
        aiMessageDao: AIMessageDao,
        notificationHelper: NotificationHelper,
        preferencesManager: PreferencesManager // ADD THIS
    ): HabitRepository {
        return HabitRepository(
            habitDao,
            habitLogDao,
            userProfileDao,
            aiMessageDao,
            notificationHelper,
            preferencesManager // ADD THIS
        )
    }
    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao {
        return database.alarmDao()
    }
}