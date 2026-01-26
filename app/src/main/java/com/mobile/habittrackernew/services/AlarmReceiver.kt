package com.mobile.habittrackernew.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("alarm_id", 0)
        val label = intent.getStringExtra("alarm_label") ?: "Alarm"
        val vibrate = intent.getBooleanExtra("alarm_vibrate", true)
        val soundUri = intent.getStringExtra("alarm_sound")
        val isSnooze = intent.getBooleanExtra("is_snooze", false)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_START_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmService.EXTRA_ALARM_VIBRATE, vibrate)
            putExtra(AlarmService.EXTRA_ALARM_SOUND, soundUri)
            putExtra(AlarmService.EXTRA_IS_SNOOZE, isSnooze)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}