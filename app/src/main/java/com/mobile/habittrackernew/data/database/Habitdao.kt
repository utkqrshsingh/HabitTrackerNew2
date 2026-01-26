package com.mobile.habittrackernew.data.database

import androidx.room.*
import com.mobile.habittrackernew.data.models.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY orderIndex ASC")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY orderIndex ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitByIdFlow(id: Long): Flow<Habit?>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // Soft delete - just mark as inactive
    @Query("UPDATE habits SET isActive = 0 WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: Long)

    @Query("SELECT MAX(orderIndex) FROM habits")
    suspend fun getMaxOrderIndex(): Int?
}