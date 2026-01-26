package com.mobile.habittrackernew.data.database

import androidx.room.*
import com.mobile.habittrackernew.data.models.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsByHabitId(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE category = :category ORDER BY date DESC")
    fun getLogsByCategory(category: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsByDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogByHabitIdAndDate(habitId: Long, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE category = :category AND date = :date LIMIT 1")
    suspend fun getLogByCategoryAndDate(category: String, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLogsByHabitIdAndDateRange(habitId: Long, startDate: String, endDate: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE category = :category AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLogsByCategoryAndDateRange(category: String, startDate: String, endDate: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedLogsByHabitId(habitId: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE category = :category AND isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedLogsByCategory(category: String): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog): Long

    @Update
    suspend fun updateLog(log: HabitLog)

    @Delete
    suspend fun deleteLog(log: HabitLog)

    // Delete all logs for a habit
    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsByHabitId(habitId: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()
}