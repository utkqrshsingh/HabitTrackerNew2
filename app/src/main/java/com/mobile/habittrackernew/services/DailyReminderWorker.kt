package com.mobile.habittrackernew.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobile.habittrackernew.HabitTrackerApplication
import com.mobile.habittrackernew.MainActivity
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, params) {

    private val morningMessages = listOf(
        "Good Morning! ☀️ Ready to crush your habits today?",
        "Rise and shine! 🌅 A new day, new opportunities!",
        "Morning champion! 💪 Let's make today count!",
        "Hello! ☀️ Your habits are waiting for you!",
        "Good morning! 🌟 Start strong, finish stronger!"
    )

    private val eveningMessages = listOf(
        "Good evening! 🌙 Don't forget to complete your habits!",
        "Evening check-in! ✨ How did your habits go today?",
        "Hey there! 🌅 Still time to finish strong!",
        "Evening reminder! 💫 Complete your remaining habits!",
        "Almost done! 🌙 Finish your habits before bed!"
    )

    override suspend fun doWork(): Result {
        // Check if notifications are enabled
        val notificationsEnabled = preferencesManager.notificationsEnabled.first()
        if (!notificationsEnabled) {
            return Result.success() // Skip silently
        }

        // Determine if it's morning or evening based on input data
        val reminderType = inputData.getString("reminder_type") ?: "morning"

        showNotification(reminderType)

        // Increment notification count
        preferencesManager.incrementNotificationCount()

        return Result.success()
    }

    private fun showNotification(reminderType: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message, notificationId) = when (reminderType) {
            "evening" -> Triple(
                "Evening Reminder 🌙",
                eveningMessages.random(),
                1003
            )
            else -> Triple(
                "Morning Motivation ☀️",
                morningMessages.random(),
                1001
            )
        }

        val notification = NotificationCompat.Builder(
            context,
            HabitTrackerApplication.REMINDER_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.notify(notificationId, notification)
    }
}