package com.mobile.habittrackernew

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for habit reminders"
            enableVibration(true)
        }

        val motivationChannel = NotificationChannel(
            MOTIVATION_CHANNEL_ID,
            "Motivation",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Motivational messages from your AI coach"
        }

        val streakChannel = NotificationChannel(
            STREAK_CHANNEL_ID,
            "Streak Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts about your habit streaks"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(
            listOf(reminderChannel, motivationChannel, streakChannel)
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        const val REMINDER_CHANNEL_ID = "habit_reminders"
        const val MOTIVATION_CHANNEL_ID = "motivation"
        const val STREAK_CHANNEL_ID = "streak_alerts"
    }
}