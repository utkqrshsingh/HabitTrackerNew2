package com.mobile.habittrackernew.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mobile.habittrackernew.data.models.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm.id)
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label)
            putExtra("alarm_vibrate", alarm.vibrate)
            putExtra("alarm_sound", alarm.soundUri)
            putExtra("is_snooze", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm)

        scheduleExactAlarm(triggerTime, pendingIntent)
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = alarm.getRepeatDaysList()

        if (repeatDays.isEmpty()) {
            // One-time alarm
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            // Repeating alarm
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            var daysToAdd = 0
            var found = false

            // Check if today is a valid day and time hasn't passed
            if (repeatDays.contains(today) && calendar.timeInMillis > System.currentTimeMillis()) {
                found = true
            }

            // Find next valid day
            if (!found) {
                for (i in 1..7) {
                    val checkDay = (today + i) % 7
                    if (repeatDays.contains(checkDay)) {
                        daysToAdd = i
                        found = true
                        break
                    }
                }
            }

            if (daysToAdd > 0) {
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
            }
        }

        return calendar.timeInMillis
    }

    fun scheduleSnooze(alarmId: Long, label: String, snoozeMinutes: Int): Long {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_label", label)
            putExtra("alarm_vibrate", true)
            putExtra("is_snooze", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(triggerTime, pendingIntent)
        return triggerTime
    }

    private fun scheduleExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)

        // Cancel main alarm
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        // Cancel snooze
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(snoozePendingIntent)
    }
}