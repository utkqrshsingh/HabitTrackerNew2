package com.mobile.habittrackernew.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class CategoryUiState(
    val categoryName: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val weekLogs: Map<String, Boolean> = emptyMap(),
    val monthLogs: List<HabitLog> = emptyList(),
    val recentLogs: List<HabitLog> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today = LocalDate.now().format(dateFormatter)

    fun loadCategory(categoryName: String) {
        _uiState.update { it.copy(categoryName = categoryName, isLoading = true) }

        viewModelScope.launch {
            val streakInfo = repository.calculateStreak(categoryName)
            val todayLog = repository.getLogByCategoryAndDate(categoryName, today)

            val weekStart = LocalDate.now().minusDays(6).format(dateFormatter)
            val monthStart = LocalDate.now().withDayOfMonth(1).format(dateFormatter)

            combine(
                repository.getLogsByCategoryAndDateRange(categoryName, weekStart, today),
                repository.getLogsByCategoryAndDateRange(categoryName, monthStart, today),
                repository.getLogsByCategory(categoryName)
            ) { weekLogs, monthLogs, allLogs ->

                val weekMap = weekLogs.associate { it.date to it.isCompleted }

                _uiState.update { state ->
                    state.copy(
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
            val categoryName = _uiState.value.categoryName
            repository.toggleHabitCompletion(categoryName, today)
            loadCategory(categoryName)
        }
    }
}