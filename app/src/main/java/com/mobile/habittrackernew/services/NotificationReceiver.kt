package com.mobile.habittrackernew.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        // Check if notifications are enabled
        val notificationsEnabled = runBlocking {
            preferencesManager.notificationsEnabled.first()
        }

        if (!notificationsEnabled) return

        val title = intent.getStringExtra("title") ?: "Habit Reminder"
        val message = intent.getStringExtra("message") ?: "Don't forget your habits!"
        val notificationId = intent.getIntExtra("notificationId", System.currentTimeMillis().toInt())

        notificationHelper.showInstantNotification(title, message, notificationId)
    }
}