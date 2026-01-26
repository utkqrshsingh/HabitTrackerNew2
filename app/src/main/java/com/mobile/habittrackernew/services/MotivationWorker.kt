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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MotivationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val motivationalMessages = listOf(
        "💪 Keep going! You're building great habits!",
        "🔥 Your streak is on fire! Don't break it!",
        "⭐ Small steps lead to big changes!",
        "🎯 Stay focused on your goals!",
        "🌟 You're doing amazing! Keep it up!",
        "💎 Consistency is the key to success!",
        "🚀 Every day is a new opportunity!",
        "🏆 Champions are made through daily habits!"
    )

    override suspend fun doWork(): Result {
        showMotivationNotification()
        return Result.success()
    }

    private fun showMotivationNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val message = motivationalMessages.random()

        val notification = NotificationCompat.Builder(
            context,
            HabitTrackerApplication.MOTIVATION_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your AI Coach")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.notify(1002, notification)
    }
}