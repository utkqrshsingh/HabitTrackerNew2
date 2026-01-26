package com.mobile.habittrackernew.ui.screens.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.models.Alarm
import com.mobile.habittrackernew.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),
    val nextAlarm: Alarm? = null,
    val isLoading: Boolean = true,
    val countdownText: String = ""
)

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    init {
        loadAlarms()
        startCountdownTimer()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            alarmRepository.getAllAlarms().collect { alarms ->
                val nextAlarm = findNextAlarm(alarms.filter { it.isEnabled })
                _uiState.update {
                    it.copy(
                        alarms = alarms,
                        nextAlarm = nextAlarm,
                        isLoading = false,
                        countdownText = calculateCountdown(nextAlarm)
                    )
                }
            }
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.value.nextAlarm?.let { alarm ->
                    _uiState.update {
                        it.copy(countdownText = calculateCountdown(alarm))
                    }
                }
            }
        }
    }

    private fun calculateCountdown(alarm: Alarm?): String {
        if (alarm == null) return ""

        val now = System.currentTimeMillis()
        val targetTime: Long = if (alarm.isSnoozed && alarm.snoozedTime != null && alarm.snoozedTime > now) {
            alarm.snoozedTime
        } else {
            getNextTriggerTime(alarm)
        }

        val diff = targetTime - now
        if (diff <= 0) return "Ringing soon..."

        val totalSeconds = diff / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 24 -> {
                val days = hours / 24
                val remainingHours = hours % 24
                if (days == 1L) "1 day ${remainingHours}h ${minutes}m"
                else "$days days ${remainingHours}h"
            }
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun getTimeRemainingForAlarm(alarm: Alarm): String {
        val now = System.currentTimeMillis()
        val targetTime: Long = if (alarm.isSnoozed && alarm.snoozedTime != null && alarm.snoozedTime > now) {
            alarm.snoozedTime
        } else {
            getNextTriggerTime(alarm)
        }

        val diff = targetTime - now
        if (diff <= 0) return "Ringing soon..."

        val totalSeconds = diff / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 24 -> {
                val days = hours / 24
                val remainingHours = hours % 24
                if (days == 1L) "1 day ${remainingHours}h ${minutes}m left"
                else "$days days left"
            }
            hours > 0 -> "${hours}h ${minutes}m left"
            minutes > 0 -> "${minutes}m left"
            else -> "Less than a minute"
        }
    }

    private fun getNextTriggerTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeatDays = alarm.getRepeatDaysList()

        if (repeatDays.isEmpty()) {
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            var daysToAdd = 0
            var found = false

            if (repeatDays.contains(today) && calendar.timeInMillis > System.currentTimeMillis()) {
                found = true
            }

            if (!found) {
                for (i in 1..7) {
                    val checkDay = (today + i) % 7
                    if (repeatDays.contains(checkDay)) {
                        daysToAdd = i
                        break
                    }
                }
            }

            if (daysToAdd > 0) {
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
            }
        }

        return calendar.timeInMillis
    }

    private fun findNextAlarm(enabledAlarms: List<Alarm>): Alarm? {
        if (enabledAlarms.isEmpty()) return null

        val now = System.currentTimeMillis()

        val snoozedAlarms = enabledAlarms.filter {
            it.isSnoozed && it.snoozedTime != null && it.snoozedTime > now
        }

        if (snoozedAlarms.isNotEmpty()) {
            val nextSnoozed = snoozedAlarms.minByOrNull { it.snoozedTime!! }
            if (nextSnoozed != null) {
                val nextRegular = findNextRegularAlarm(enabledAlarms.filter { !it.isSnoozed })
                return if (nextRegular == null || nextSnoozed.snoozedTime!! < getNextTriggerTime(nextRegular)) {
                    nextSnoozed
                } else {
                    nextRegular
                }
            }
        }

        return findNextRegularAlarm(enabledAlarms)
    }

    private fun findNextRegularAlarm(alarms: List<Alarm>): Alarm? {
        if (alarms.isEmpty()) return null

        return alarms.minByOrNull { getNextTriggerTime(it) }
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarmId)
        }
    }

    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        viewModelScope.launch {
            alarmRepository.toggleAlarm(alarmId, enabled)
        }
    }

    fun snoozeAlarm(alarmId: Long, snoozeMinutes: Int = 5) {
        viewModelScope.launch {
            alarmRepository.snoozeAlarm(alarmId, snoozeMinutes)
        }
    }

    fun dismissAlarm(alarmId: Long) {
        viewModelScope.launch {
            alarmRepository.dismissAlarm(alarmId)
        }
    }

    fun clearSnooze(alarmId: Long) {
        viewModelScope.launch {
            alarmRepository.resetSnooze(alarmId)
        }
    }
}