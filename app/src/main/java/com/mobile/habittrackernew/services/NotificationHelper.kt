package com.mobile.habittrackernew.services

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mobile.habittrackernew.HabitTrackerApplication
import com.mobile.habittrackernew.MainActivity
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class ScheduledNotification(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val isEnabled: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    MORNING_REMINDER,
    EVENING_REMINDER,
    HABIT_REMINDER,
    STREAK_ALERT,
    MOTIVATION
}

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "NotificationHelper"
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun areNotificationsEnabled(): Boolean {
        return runBlocking { preferencesManager.notificationsEnabled.first() }
    }

    fun showInstantNotification(
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled in preferences")
            return
        }
        if (!hasNotificationPermission()) {
            Log.d(TAG, "No notification permission")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HabitTrackerApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown: $title")

        coroutineScope.launch {
            preferencesManager.incrementNotificationCount()
        }
    }

    fun scheduleNotification(
        title: String,
        message: String,
        hour: Int,
        minute: Int,
        notificationId: Int
    ) {
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled, not scheduling")
            return
        }

        Log.d(TAG, "Scheduling notification: $title at $hour:$minute (ID: $notificationId)")

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            // Cancel any existing alarm first
            alarmManager.cancel(pendingIntent)

            // Use setAlarmClock for reliable delivery (shows alarm icon in status bar)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled with setAlarmClock")
                } else {
                    // Fallback for when exact alarms not permitted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled with setAndAllowWhileIdle")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
                Log.d(TAG, "Scheduled with setAlarmClock (pre-S)")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled with setExact")
            }

            Log.d(TAG, "Notification scheduled for: ${calendar.time}")

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification", e)
        }
    }

    fun cancelNotification(notificationId: Int) {
        Log.d(TAG, "Cancelling notification ID: $notificationId")

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications() {
        cancelNotification(1001) // Morning
        cancelNotification(1002) // Midday
        cancelNotification(1003) // Evening
        notificationManager.cancelAll()
    }

    fun getScheduledNotifications(): List<ScheduledNotification> {
        val notificationsEnabled = areNotificationsEnabled()
        val morningTime = runBlocking { preferencesManager.morningReminder.first() }
        val eveningTime = runBlocking { preferencesManager.eveningReminder.first() }

        return listOf(
            ScheduledNotification(
                id = 1001,
                title = "Morning Motivation",
                message = "Rise and shine! Time to crush your habits today! ☀️",
                time = morningTime,
                isEnabled = notificationsEnabled,
                type = NotificationType.MORNING_REMINDER
            ),
            ScheduledNotification(
                id = 1002,
                title = "Midday Check-in",
                message = "How's your day going? Don't forget your habits! 💪",
                time = "13:00",
                isEnabled = notificationsEnabled,
                type = NotificationType.HABIT_REMINDER
            ),
            ScheduledNotification(
                id = 1003,
                title = "Evening Reminder",
                message = "End your day strong! Complete your remaining habits 🌙",
                time = eveningTime,
                isEnabled = notificationsEnabled,
                type = NotificationType.EVENING_REMINDER
            )
        )
    }

    suspend fun clearNotificationCount() {
        preferencesManager.clearNotificationCount()
    }
}