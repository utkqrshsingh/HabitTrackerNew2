package com.mobile.habittrackernew.data.repository

import com.mobile.habittrackernew.data.database.AIMessageDao
import com.mobile.habittrackernew.data.database.HabitDao
import com.mobile.habittrackernew.data.database.HabitLogDao
import com.mobile.habittrackernew.data.database.UserProfileDao
import com.mobile.habittrackernew.data.models.*
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val userProfileDao: UserProfileDao,
    private val aiMessageDao: AIMessageDao,
    private val notificationHelper: NotificationHelper,
    private val preferencesManager: PreferencesManager // ADD THIS
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ==================== HABIT OPERATIONS ====================

    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    fun getHabitByIdFlow(id: Long): Flow<Habit?> = habitDao.getHabitByIdFlow(id)

    suspend fun insertHabit(habit: Habit): Long {
        val maxOrder = habitDao.getMaxOrderIndex() ?: -1
        val habitWithOrder = habit.copy(orderIndex = maxOrder + 1)
        val habitId = habitDao.insertHabit(habitWithOrder)

        // Schedule reminder if enabled
        habit.reminderTime?.let { time ->
            scheduleHabitReminder(habitId, habit.name, time)
        }

        return habitId
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)

        // Cancel existing reminder for this habit
        cancelHabitReminder(habit.id)

        // Schedule new reminder if enabled
        habit.reminderTime?.let { time ->
            scheduleHabitReminder(habit.id, habit.name, time)
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        // Cancel reminder first
        cancelHabitReminder(habitId)

        // Delete associated logs
        habitLogDao.deleteLogsByHabitId(habitId)

        // Delete the habit
        habitDao.deleteHabitById(habitId)
    }

    suspend fun softDeleteHabit(habitId: Long) {
        // Cancel reminder
        cancelHabitReminder(habitId)

        // Soft delete (mark as inactive)
        habitDao.softDeleteHabit(habitId)
    }

    suspend fun deleteAllHabits() {
        cancelAllHabitReminders()
        habitDao.deleteAllHabits()
    }

    // UPDATED: Only seed once, never again
    suspend fun seedDefaultHabitsIfNeeded() {
        val hasSeeded = preferencesManager.hasSeededHabits.first()

        // If already seeded before, don't seed again
        if (hasSeeded) {
            return
        }

        // First time - seed default habits
        val count = habitDao.getHabitCount()
        if (count == 0) {
            habitDao.insertHabits(DefaultHabits.habits)
        }

        // Mark as seeded so it never happens again
        preferencesManager.setHasSeededHabits(true)
    }

    // ==================== HABIT REMINDER OPERATIONS ====================

    private fun scheduleHabitReminder(habitId: Long, habitName: String, time: String) {
        val (hour, minute) = parseTime(time)

        notificationHelper.scheduleNotification(
            title = "⏰ Habit Reminder",
            message = "Time for your habit: $habitName",
            hour = hour,
            minute = minute,
            notificationId = getHabitNotificationId(habitId)
        )
    }

    private fun cancelHabitReminder(habitId: Long) {
        notificationHelper.cancelNotification(getHabitNotificationId(habitId))
    }

    private fun getHabitNotificationId(habitId: Long): Int {
        return (habitId + 2000).toInt()
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        return try {
            val parts = time.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(9, 0)
        }
    }

    suspend fun rescheduleAllHabitReminders() {
        val habits = habitDao.getAllActiveHabits().first()
        habits.forEach { habit ->
            habit.reminderTime?.let { time ->
                scheduleHabitReminder(habit.id, habit.name, time)
            }
        }
    }

    suspend fun cancelAllHabitReminders() {
        val habits = habitDao.getAllActiveHabits().first()
        habits.forEach { habit ->
            cancelHabitReminder(habit.id)
        }
    }

    // ==================== HABIT LOG OPERATIONS ====================

    fun getLogsByHabitId(habitId: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsByHabitId(habitId)

    fun getLogsByCategory(category: String): Flow<List<HabitLog>> =
        habitLogDao.getLogsByCategory(category)

    fun getLogsByDate(date: String): Flow<List<HabitLog>> =
        habitLogDao.getLogsByDate(date)

    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<HabitLog>> =
        habitLogDao.getLogsByDateRange(startDate, endDate)

    fun getLogsByHabitIdAndDateRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<HabitLog>> =
        habitLogDao.getLogsByHabitIdAndDateRange(habitId, startDate, endDate)

    fun getLogsByCategoryAndDateRange(
        category: String,
        startDate: String,
        endDate: String
    ): Flow<List<HabitLog>> =
        habitLogDao.getLogsByCategoryAndDateRange(category, startDate, endDate)

    fun getAllLogs(): Flow<List<HabitLog>> = habitLogDao.getAllLogs()

    suspend fun getLogByHabitIdAndDate(habitId: Long, date: String): HabitLog? =
        habitLogDao.getLogByHabitIdAndDate(habitId, date)

    suspend fun getLogByCategoryAndDate(category: String, date: String): HabitLog? =
        habitLogDao.getLogByCategoryAndDate(category, date)

    suspend fun toggleHabitCompletion(
        habitId: Long,
        habitName: String,
        date: String,
        duration: Int = 30
    ): Boolean {
        val existingLog = habitLogDao.getLogByHabitIdAndDate(habitId, date)
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
                habitId = habitId,
                category = habitName,
                date = date,
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                duration = duration
            )
            habitLogDao.insertLog(newLog)
            true
        }
    }

    // Legacy method for backward compatibility
    suspend fun toggleHabitCompletion(
        category: String,
        date: String,
        duration: Int = 30
    ): Boolean {
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

    suspend fun insertLog(log: HabitLog): Long = habitLogDao.insertLog(log)

    suspend fun updateLog(log: HabitLog) = habitLogDao.updateLog(log)

    suspend fun deleteLog(log: HabitLog) = habitLogDao.deleteLog(log)

    suspend fun deleteAllHabitLogs() {
        habitLogDao.deleteAllLogs()
    }

    // ==================== STREAK CALCULATIONS ====================

    suspend fun calculateStreakForHabit(habitId: Long): StreakInfo {
        val habit = habitDao.getHabitById(habitId) ?: return StreakInfo("", 0, 0, null)
        val logs = habitLogDao.getCompletedLogsByHabitId(habitId)
        return calculateStreakFromLogs(habit.name, logs)
    }

    suspend fun calculateStreak(category: String): StreakInfo {
        val logs = habitLogDao.getCompletedLogsByCategory(category)
        return calculateStreakFromLogs(category, logs)
    }

    private fun calculateStreakFromLogs(name: String, logs: List<HabitLog>): StreakInfo {
        if (logs.isEmpty()) return StreakInfo(name, 0, 0, null)

        val sortedDates = logs.mapNotNull { log ->
            try {
                LocalDate.parse(log.date, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.sortedDescending()

        if (sortedDates.isEmpty()) return StreakInfo(name, 0, 0, null)

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
            category = name,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = sortedDates.firstOrNull()?.format(dateFormatter)
        )
    }

    suspend fun getAllStreaksForHabits(): Map<Long, StreakInfo> {
        val habits = habitDao.getAllActiveHabits().first()
        return habits.associate { habit ->
            habit.id to calculateStreakForHabit(habit.id)
        }
    }

    suspend fun getAllStreaks(): Map<String, StreakInfo> {
        return Category.values().associate { category ->
            category.name to calculateStreak(category.name)
        }
    }

    // ==================== STATISTICS ====================

    suspend fun getTotalCompletedToday(): Int {
        val today = LocalDate.now().format(dateFormatter)
        val logs = habitLogDao.getLogsByDate(today).first()
        return logs.count { it.isCompleted }
    }

    suspend fun getTotalCompletedThisWeek(): Int {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6).format(dateFormatter)
        val todayStr = today.format(dateFormatter)
        val logs = habitLogDao.getLogsByDateRange(weekStart, todayStr).first()
        return logs.count { it.isCompleted }
    }

    suspend fun getTotalCompletedThisMonth(): Int {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        val todayStr = today.format(dateFormatter)
        val logs = habitLogDao.getLogsByDateRange(monthStart, todayStr).first()
        return logs.count { it.isCompleted }
    }

    suspend fun getCompletionRateForHabit(habitId: Long, days: Int = 30): Float {
        val today = LocalDate.now()
        val startDate = today.minusDays(days.toLong() - 1).format(dateFormatter)
        val todayStr = today.format(dateFormatter)

        val logs = habitLogDao.getLogsByHabitIdAndDateRange(habitId, startDate, todayStr).first()
        val completedDays = logs.count { it.isCompleted }

        return if (days > 0) completedDays.toFloat() / days else 0f
    }

    suspend fun getOverallCompletionRate(days: Int = 30): Float {
        val habits = habitDao.getAllActiveHabits().first()
        if (habits.isEmpty()) return 0f

        val rates = habits.map { habit ->
            getCompletionRateForHabit(habit.id, days)
        }

        return rates.average().toFloat()
    }

    // ==================== USER PROFILE ====================

    fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileSync(): UserProfile? = userProfileDao.getUserProfileSync()

    suspend fun saveUserProfile(profile: UserProfile) = userProfileDao.insertProfile(profile)

    suspend fun updateUserProfile(profile: UserProfile) = userProfileDao.updateProfile(profile)

    // ==================== AI MESSAGES ====================

    fun getAIMessages(): Flow<List<AIMessage>> = aiMessageDao.getAllMessages()

    suspend fun getRecentMessages(limit: Int): List<AIMessage> =
        aiMessageDao.getRecentMessages(limit)

    suspend fun saveAIMessage(message: AIMessage): Long = aiMessageDao.insertMessage(message)

    suspend fun clearAIMessages() = aiMessageDao.deleteAllMessages()

    // ==================== DATA MANAGEMENT ====================

    suspend fun clearAllData() {
        // Cancel all habit reminders
        cancelAllHabitReminders()

        // Clear all tables
        aiMessageDao.deleteAllMessages()
    }

    suspend fun exportData(): Map<String, Any> {
        val habits = habitDao.getAllActiveHabits().first()
        val logs = habitLogDao.getAllLogs().first()
        val profile = userProfileDao.getUserProfileSync()

        return mapOf(
            "habits" to habits,
            "logs" to logs,
            "profile" to (profile ?: emptyMap<String, Any>()),
            "exportDate" to System.currentTimeMillis()
        )
    }
}