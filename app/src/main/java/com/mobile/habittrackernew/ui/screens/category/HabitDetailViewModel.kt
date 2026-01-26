package com.mobile.habittrackernew.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Habit
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HabitDetailUiState(
    val habit: Habit? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val weekLogs: Map<String, Boolean> = emptyMap(),
    val monthLogs: List<HabitLog> = emptyList(),
    val recentLogs: List<HabitLog> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today = LocalDate.now().format(dateFormatter)

    private var currentHabitId: Long = 0

    fun loadHabit(habitId: Long) {
        currentHabitId = habitId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val habit = repository.getHabitById(habitId)

            if (habit == null) {
                _uiState.update { it.copy(habit = null, isLoading = false) }
                return@launch
            }

            val streakInfo = repository.calculateStreakForHabit(habitId)
            val todayLog = repository.getLogByHabitIdAndDate(habitId, today)

            val weekStart = LocalDate.now().minusDays(6).format(dateFormatter)
            val monthStart = LocalDate.now().withDayOfMonth(1).format(dateFormatter)

            combine(
                repository.getLogsByHabitIdAndDateRange(habitId, weekStart, today),
                repository.getLogsByHabitIdAndDateRange(habitId, monthStart, today),
                repository.getLogsByHabitId(habitId)
            ) { weekLogs, monthLogs, allLogs ->

                val weekMap = weekLogs.associate { it.date to it.isCompleted }

                _uiState.update { state ->
                    state.copy(
                        habit = habit,
                        currentStreak = streakInfo.currentStreak,
                        longestStreak = streakInfo.longestStreak,
                        isCompletedToday = todayLog?.isCompleted ?: false,
                        weekLogs = weekMap,
                        monthLogs = monthLogs,
                        recentLogs = allLogs.sortedByDescending { it.date },
                        isLoading = false
                    )
                }
            }.collect { }
        }
    }

    fun toggleTodayCompletion() {
        viewModelScope.launch {
            val habit = _uiState.value.habit ?: return@launch
            repository.toggleHabitCompletion(habit.id, habit.name, today)
            loadHabit(currentHabitId)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            loadHabit(habit.id)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit.id)
        }
    }
}