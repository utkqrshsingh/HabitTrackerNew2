package com.mobile.habittrackernew.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Category
import com.mobile.habittrackernew.data.models.HabitLog
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ProgressUiState(
    val totalCompleted: Int = 0,
    val streaks: Map<String, Int> = emptyMap(),
    val completionRates: Map<String, Float> = emptyMap(),
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

            val allStreaks = repository.getAllStreaks()
            val streakMap = allStreaks.mapValues { it.value.currentStreak }

            val today = LocalDate.now()
            val thirtyDaysAgo = today.minusDays(30)

            repository.getLogsByDateRange(
                thirtyDaysAgo.format(dateFormatter),
                today.format(dateFormatter)
            ).collect { logs ->
                val totalCompleted = logs.count { it.isCompleted }

                val completionRates = Category.entries.associate { category ->
                    val categoryLogs = logs.filter { it.category == category.name }
                    val completed = categoryLogs.count { it.isCompleted }
                    val total = 30
                    category.name to (completed.toFloat() / total)
                }

                val dailyBreakdown = logs.groupBy { it.date }

                _uiState.update {
                    it.copy(
                        totalCompleted = totalCompleted,
                        streaks = streakMap,
                        completionRates = completionRates,
                        monthLogs = logs,
                        dailyBreakdown = dailyBreakdown,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            val startDate = yearMonth.atDay(1).format(dateFormatter)
            val endDate = yearMonth.atEndOfMonth().format(dateFormatter)

            repository.getLogsByDateRange(startDate, endDate).collect { logs ->
                _uiState.update {
                    it.copy(monthLogs = logs)
                }
            }
        }
    }
}