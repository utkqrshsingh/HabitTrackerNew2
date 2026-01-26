package com.mobile.habittrackernew.di

import android.content.Context
import androidx.room.Room
import com.mobile.habittrackernew.data.database.AIMessageDao
import com.mobile.habittrackernew.data.database.AppDatabase
import com.mobile.habittrackernew.data.database.HabitDao
import com.mobile.habittrackernew.data.database.HabitLogDao
import com.mobile.habittrackernew.data.database.UserProfileDao
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.AIService
import com.mobile.habittrackernew.services.GoogleAuthHelper
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
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
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitLogDao: HabitLogDao,
        userProfileDao: UserProfileDao,
        aiMessageDao: AIMessageDao
    ): HabitRepository {
        return HabitRepository(habitDao, habitLogDao, userProfileDao, aiMessageDao)
    }

    @Provides
    @Singleton
    fun provideAIService(): AIService {
        return AIService()
    }

    @Provides
    @Singleton
    fun provideGoogleAuthHelper(@ApplicationContext context: Context): GoogleAuthHelper {
        return GoogleAuthHelper(context)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
}