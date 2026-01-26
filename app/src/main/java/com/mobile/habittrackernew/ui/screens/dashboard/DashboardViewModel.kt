package com.mobile.habittrackernew.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Habit
import com.mobile.habittrackernew.data.models.StreakInfo
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DashboardUiState(
    val habits: List<Habit> = emptyList(),
    val streaks: Map<Long, StreakInfo> = emptyMap(),
    val todayCompletions: Map<Long, Boolean> = emptyMap(),
    val todayCompletedCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today = LocalDate.now().format(dateFormatter)

    init {
        seedDefaultHabitsAndLoad()
    }

    private fun seedDefaultHabitsAndLoad() {
        viewModelScope.launch {
            repository.seedDefaultHabitsIfNeeded()
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getAllActiveHabits().collect { habits ->
                val streaks = repository.getAllStreaksForHabits()
                val completions = mutableMapOf<Long, Boolean>()

                habits.forEach { habit ->
                    val log = repository.getLogByHabitIdAndDate(habit.id, today)
                    completions[habit.id] = log?.isCompleted ?: false
                }

                val completedCount = completions.values.count { it }

                _uiState.update {
                    it.copy(
                        habits = habits,
                        streaks = streaks,
                        todayCompletions = completions,
                        todayCompletedCount = completedCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            val isCompleted = repository.toggleHabitCompletion(habit.id, habit.name, today)

            _uiState.update { state ->
                val updatedCompletions = state.todayCompletions.toMutableMap()
                updatedCompletions[habit.id] = isCompleted

                val newStreak = repository.calculateStreakForHabit(habit.id)
                val updatedStreaks = state.streaks.toMutableMap()
                updatedStreaks[habit.id] = newStreak

                state.copy(
                    todayCompletions = updatedCompletions,
                    todayCompletedCount = updatedCompletions.values.count { it },
                    streaks = updatedStreaks
                )
            }
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
            // Data will auto-refresh through Flow
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            // Data will auto-refresh through Flow
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit.id)
            // Data will auto-refresh through Flow
        }
    }

    fun refresh() {
        loadData()
    }
}