package com.mobile.habittrackernew.data.database

import androidx.room.*
import com.mobile.habittrackernew.data.models.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    fun getEnabledAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): Alarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteAlarmById(alarmId: Long)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :alarmId")
    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmCount(): Int

    // Snooze related queries
    @Query("UPDATE alarms SET snoozeCount = snoozeCount + 1, snoozedTime = :snoozedTime, isSnoozed = 1 WHERE id = :alarmId")
    suspend fun incrementSnoozeCount(alarmId: Long, snoozedTime: Long)

    @Query("UPDATE alarms SET snoozeCount = 0, snoozedTime = NULL, isSnoozed = 0 WHERE id = :alarmId")
    suspend fun resetSnooze(alarmId: Long)

    @Query("UPDATE alarms SET isSnoozed = 0, snoozedTime = NULL WHERE id = :alarmId")
    suspend fun clearSnoozedState(alarmId: Long)
}