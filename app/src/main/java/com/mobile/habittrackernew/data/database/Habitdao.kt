package com.mobile.habittrackernew.data.database

import androidx.room.*
import com.mobile.habittrackernew.data.models.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1")
    fun getAllActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE category = :category AND isActive = 1")
    fun getHabitsByCategory(category: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
}