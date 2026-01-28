package com.mobile.habittrackernew.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Notification received!")

        // Check if notifications are enabled
        val notificationsEnabled = runBlocking {
            preferencesManager.notificationsEnabled.first()
        }

        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications disabled, skipping")
            return
        }

        val title = intent.getStringExtra("title") ?: "Habit Reminder"
        val message = intent.getStringExtra("message") ?: "Don't forget your habits!"
        val notificationId = intent.getIntExtra("notificationId", System.currentTimeMillis().toInt())

        Log.d(TAG, "Showing notification: $title (ID: $notificationId)")

        // Show the notification
        notificationHelper.showInstantNotification(title, message, notificationId)

        // Reschedule for tomorrow (since we're using setAlarmClock which is one-time)
        rescheduleForTomorrow(notificationId, title, message)
    }

    private fun rescheduleForTomorrow(notificationId: Int, title: String, message: String) {
        try {
            val time = when (notificationId) {
                1001 -> runBlocking { preferencesManager.morningReminder.first() }
                1003 -> runBlocking { preferencesManager.eveningReminder.first() }
                else -> return
            }

            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Schedule for tomorrow
            notificationHelper.scheduleNotification(
                title = title,
                message = message,
                hour = hour,
                minute = minute,
                notificationId = notificationId
            )

            Log.d(TAG, "Rescheduled notification for tomorrow: $time")

        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling notification", e)
        }
    }
}