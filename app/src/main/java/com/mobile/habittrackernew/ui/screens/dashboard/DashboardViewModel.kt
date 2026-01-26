package com.mobile.habittrackernew.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Category
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
    val streaks: Map<String, StreakInfo> = emptyMap(),
    val todayCompletions: Map<String, Boolean> = emptyMap(),
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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val streaks = repository.getAllStreaks()

            repository.getLogsByDate(today).collect { logs ->
                val completions = mutableMapOf<String, Boolean>()

                Category.values().forEach { category ->
                    val log = logs.find { it.category == category.name }
                    completions[category.name] = log?.isCompleted ?: false
                }

                val completedCount = completions.values.count { it }

                _uiState.update {
                    it.copy(
                        streaks = streaks,
                        todayCompletions = completions,
                        todayCompletedCount = completedCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleHabitCompletion(category: String) {
        viewModelScope.launch {
            val isCompleted = repository.toggleHabitCompletion(category, today)

            _uiState.update { state ->
                val updatedCompletions = state.todayCompletions.toMutableMap()
                updatedCompletions[category] = isCompleted

                val newStreak = repository.calculateStreak(category)
                val updatedStreaks = state.streaks.toMutableMap()
                updatedStreaks[category] = newStreak

                state.copy(
                    todayCompletions = updatedCompletions,
                    todayCompletedCount = updatedCompletions.values.count { it },
                    streaks = updatedStreaks
                )
            }
        }
    }

    fun refresh() {
        loadData()
    }
}