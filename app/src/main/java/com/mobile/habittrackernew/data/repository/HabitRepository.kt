package com.mobile.habittrackernew.data.repository

import com.mobile.habittrackernew.data.database.AIMessageDao
import com.mobile.habittrackernew.data.database.HabitDao
import com.mobile.habittrackernew.data.database.HabitLogDao
import com.mobile.habittrackernew.data.database.UserProfileDao
import com.mobile.habittrackernew.data.models.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val userProfileDao: UserProfileDao,
    private val aiMessageDao: AIMessageDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()
    fun getHabitsByCategory(category: String): Flow<List<Habit>> = habitDao.getHabitsByCategory(category)
    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    fun getLogsByCategory(category: String): Flow<List<HabitLog>> = habitLogDao.getLogsByCategory(category)
    fun getLogsByDate(date: String): Flow<List<HabitLog>> = habitLogDao.getLogsByDate(date)
    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<HabitLog>> = habitLogDao.getLogsByDateRange(startDate, endDate)
    fun getLogsByCategoryAndDateRange(category: String, startDate: String, endDate: String): Flow<List<HabitLog>> = habitLogDao.getLogsByCategoryAndDateRange(category, startDate, endDate)
    fun getAllLogs(): Flow<List<HabitLog>> = habitLogDao.getAllLogs()
    suspend fun getLogByCategoryAndDate(category: String, date: String): HabitLog? = habitLogDao.getLogByCategoryAndDate(category, date)

    suspend fun toggleHabitCompletion(category: String, date: String, duration: Int = 30): Boolean {
        val existingLog = habitLogDao.getLogByCategoryAndDate(category, date)
        return if (existingLog != null) {
            val updatedLog = existingLog.copy(
                isCompleted = !existingLog.isCompleted,
                completedAt = if (!existingLog.isCompleted) System.currentTimeMillis() else null,
                duration = if (!existingLog.isCompleted) duration else 0
            )
            habitLogDao.updateLog(updatedLog)
            updatedLog.isCompleted
        } else {
            val newLog = HabitLog(
                habitId = 0,
                category = category,
                date = date,
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                duration = duration
            )
            habitLogDao.insertLog(newLog)
            true
        }
    }

    suspend fun calculateStreak(category: String): StreakInfo {
        val logs = habitLogDao.getCompletedLogsByCategory(category)
        if (logs.isEmpty()) return StreakInfo(category, 0, 0, null)

        val sortedDates = logs.map { LocalDate.parse(it.date, dateFormatter) }.sortedDescending()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var previousDate: LocalDate? = null

        for (date in sortedDates) {
            if (previousDate == null) {
                if (date == today || date == yesterday) {
                    tempStreak = 1
                    currentStreak = 1
                } else {
                    tempStreak = 1
                }
            } else {
                if (previousDate.minusDays(1) == date) {
                    tempStreak++
                    if (sortedDates.first() == today || sortedDates.first() == yesterday) {
                        currentStreak = tempStreak
                    }
                } else {
                    longestStreak = maxOf(longestStreak, tempStreak)
                    tempStreak = 1
                }
            }
            previousDate = date
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        return StreakInfo(
            category = category,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = sortedDates.firstOrNull()?.format(dateFormatter)
        )
    }

    suspend fun getAllStreaks(): Map<String, StreakInfo> {
        return Category.values().associate { category ->
            category.name to calculateStreak(category.name)
        }
    }

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()
    suspend fun getUserProfileSync(): UserProfile? = userProfileDao.getUserProfileSync()
    suspend fun saveUserProfile(profile: UserProfile) = userProfileDao.insertProfile(profile)

    fun getAIMessages(): Flow<List<AIMessage>> = aiMessageDao.getAllMessages()
    suspend fun getRecentMessages(limit: Int): List<AIMessage> = aiMessageDao.getRecentMessages(limit)
    suspend fun saveAIMessage(message: AIMessage): Long = aiMessageDao.insertMessage(message)
    suspend fun clearAIMessages() = aiMessageDao.clearAllMessages()
}