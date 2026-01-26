package com.mobile.habittrackernew.data.repository

import com.mobile.habittrackernew.data.database.AlarmDao
import com.mobile.habittrackernew.data.models.Alarm
import com.mobile.habittrackernew.services.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabledAlarms()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: Alarm): Long {
        val alarmId = alarmDao.insertAlarm(alarm)
        if (alarm.isEnabled) {
            val savedAlarm = alarm.copy(id = alarmId)
            alarmScheduler.scheduleAlarm(savedAlarm)
        }
        return alarmId
    }

    suspend fun updateAlarm(alarm: Alarm) {
        // Reset snooze when alarm is manually updated
        val updatedAlarm = alarm.copy(snoozeCount = 0, snoozedTime = null, isSnoozed = false)
        alarmDao.updateAlarm(updatedAlarm)
        alarmScheduler.cancelAlarm(alarm.id)
        if (updatedAlarm.isEnabled) {
            alarmScheduler.scheduleAlarm(updatedAlarm)
        }
    }

    suspend fun deleteAlarm(alarmId: Long) {
        alarmScheduler.cancelAlarm(alarmId)
        alarmDao.deleteAlarmById(alarmId)
    }

    suspend fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        alarmDao.setAlarmEnabled(alarmId, enabled)
        // Reset snooze when toggling alarm
        alarmDao.resetSnooze(alarmId)

        val alarm = alarmDao.getAlarmById(alarmId)
        alarm?.let {
            if (enabled) {
                alarmScheduler.scheduleAlarm(it)
            } else {
                alarmScheduler.cancelAlarm(alarmId)
            }
        }
    }

    suspend fun snoozeAlarm(alarmId: Long, snoozeMinutes: Int): Long {
        val snoozedTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        alarmDao.incrementSnoozeCount(alarmId, snoozedTime)
        
        val alarm = alarmDao.getAlarmById(alarmId)
        val label = alarm?.label ?: "Snooze"
        
        alarmScheduler.scheduleSnooze(alarmId, label, snoozeMinutes)
        return snoozedTime
    }

    suspend fun dismissAlarm(alarmId: Long) {
        alarmScheduler.cancelAlarm(alarmId)

        val alarm = alarmDao.getAlarmById(alarmId)
        alarm?.let {
            // Reset snooze state
            alarmDao.resetSnooze(alarmId)

            // If it's a one-time alarm, disable it
            if (it.getRepeatDaysList().isEmpty()) {
                alarmDao.setAlarmEnabled(alarmId, false)
            } else {
                // For repeating alarms, schedule the next occurrence
                alarmScheduler.scheduleAlarm(it.copy(snoozeCount = 0, snoozedTime = null, isSnoozed = false))
            }
        }
    }

    suspend fun resetSnooze(alarmId: Long) {
        alarmDao.resetSnooze(alarmId)
    }

    suspend fun getAlarmCount(): Int = alarmDao.getAlarmCount()
}
