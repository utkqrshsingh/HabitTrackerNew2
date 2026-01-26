package com.mobile.habittrackernew.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Habit
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ProgressUiState(
    val habits: List<Habit> = emptyList(),
    val totalCompleted: Int = 0,
    val streaks: Map<Long, Int> = emptyMap(),
    val completionRates: Map<Long, Float> = emptyMap(),
    val monthLogs: List<HabitLog> = emptyList(),
    val dailyBreakdown: Map<String, List<HabitLog>> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get user's habits
            repository.getAllActiveHabits().collect { habits ->
                if (habits.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            habits = emptyList(),
                            isLoading = false
                        )
                    }
                    return@collect
                }

                // Calculate streaks for each habit
                val streaks = mutableMapOf<Long, Int>()
                val completionRates = mutableMapOf<Long, Float>()

                habits.forEach { habit ->
                    val streakInfo = repository.calculateStreakForHabit(habit.id)
                    streaks[habit.id] = streakInfo.currentStreak

                    val rate = repository.getCompletionRateForHabit(habit.id, 30)
                    completionRates[habit.id] = rate
                }

                // Get all logs for total completed
                val allLogs = repository.getAllLogs().first()
                val totalCompleted = allLogs.count { it.isCompleted }

                // Get month logs
                val currentMonth = YearMonth.now()
                val monthStart = currentMonth.atDay(1).format(dateFormatter)
                val monthEnd = currentMonth.atEndOfMonth().format(dateFormatter)
                val monthLogs = repository.getLogsByDateRange(monthStart, monthEnd).first()

                // Daily breakdown (last 14 days)
                val today = LocalDate.now()
                val twoWeeksAgo = today.minusDays(13)
                val recentLogs = repository.getLogsByDateRange(
                    twoWeeksAgo.format(dateFormatter),
                    today.format(dateFormatter)
                ).first()

                val dailyBreakdown = recentLogs.groupBy { it.date }

                _uiState.update {
                    it.copy(
                        habits = habits,
                        totalCompleted = totalCompleted,
                        streaks = streaks,
                        completionRates = completionRates,
                        monthLogs = monthLogs,
                        dailyBreakdown = dailyBreakdown,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            val monthStart = yearMonth.atDay(1).format(dateFormatter)
            val monthEnd = yearMonth.atEndOfMonth().format(dateFormatter)
            val monthLogs = repository.getLogsByDateRange(monthStart, monthEnd).first()

            _uiState.update {
                it.copy(monthLogs = monthLogs)
            }
        }
    }
}