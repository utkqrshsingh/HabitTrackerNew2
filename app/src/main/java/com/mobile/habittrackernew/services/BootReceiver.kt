package com.mobile.habittrackernew.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workManager = WorkManager.getInstance(context)

            val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                1, TimeUnit.DAYS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyReminderRequest
            )

            val motivationRequest = PeriodicWorkRequestBuilder<MotivationWorker>(
                4, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "motivation_check",
                ExistingPeriodicWorkPolicy.KEEP,
                motivationRequest
            )
        }
    }
}