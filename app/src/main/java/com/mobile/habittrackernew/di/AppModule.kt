package com.mobile.habittrackernew.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.mobile.habittrackernew.data.database.*
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.AuthRepository
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

    // ============ DATABASE ============
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

    // ============ FIREBASE ============
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // ============ AUTH ============
    @Provides
    @Singleton
    fun provideGoogleAuthHelper(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): GoogleAuthHelper = GoogleAuthHelper(context, firebaseAuth)

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository = AuthRepository(firebaseAuth)

    // ============ AI SERVICE ============
    @Provides
    @Singleton
    fun provideAIService(
        @ApplicationContext context: Context
    ): AIService = AIService(context)

    // ============ PREFERENCES ============
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    // ============ DAOs ============
    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideHabitLogDao(database: AppDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    @Singleton
    fun provideAIMessageDao(database: AppDatabase): AIMessageDao = database.aiMessageDao()

    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()

    // ============ HELPERS ============
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): NotificationHelper = NotificationHelper(context, preferencesManager)

    // ============ REPOSITORIES ============
    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitLogDao: HabitLogDao,
        userProfileDao: UserProfileDao,
        aiMessageDao: AIMessageDao,
        notificationHelper: NotificationHelper,
        preferencesManager: PreferencesManager
    ): HabitRepository {
        return HabitRepository(
            habitDao,
            habitLogDao,
            userProfileDao,
            aiMessageDao,
            notificationHelper,
            preferencesManager
        )
    }
}
