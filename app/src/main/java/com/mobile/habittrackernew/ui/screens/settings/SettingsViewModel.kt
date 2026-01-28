package com.mobile.habittrackernew.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.HabitRepository
import com.mobile.habittrackernew.services.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val morningReminder: String = "08:00",
    val eveningReminder: String = "20:00",
    val showLogoutDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val logoutSuccess: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val habitRepository: HabitRepository,
    private val notificationHelper: NotificationHelper // 👈 ADD THIS
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    companion object {
        const val MORNING_NOTIFICATION_ID = 1001
        const val EVENING_NOTIFICATION_ID = 1003
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            launch {
                preferencesManager.userName.collect { name ->
                    _uiState.update { it.copy(userName = name) }
                }
            }
            launch {
                preferencesManager.userEmail.collect { email ->
                    _uiState.update { it.copy(userEmail = email) }
                }
            }
            launch {
                preferencesManager.isDarkMode.collect { isDark ->
                    _uiState.update { it.copy(isDarkMode = isDark) }
                }
            }
            launch {
                preferencesManager.notificationsEnabled.collect { enabled ->
                    _uiState.update { it.copy(notificationsEnabled = enabled) }
                    // Schedule or cancel notifications based on toggle
                    if (enabled) {
                        scheduleAllReminders()
                    } else {
                        cancelAllReminders()
                    }
                }
            }
            launch {
                preferencesManager.morningReminder.collect { time ->
                    _uiState.update { it.copy(morningReminder = time) }
                }
            }
            launch {
                preferencesManager.eveningReminder.collect { time ->
                    _uiState.update { it.copy(eveningReminder = time) }
                }
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkMode
            preferencesManager.setDarkMode(newValue)
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationsEnabled
            preferencesManager.setNotificationsEnabled(newValue)

            if (newValue) {
                scheduleAllReminders()
            } else {
                cancelAllReminders()
            }
        }
    }

    fun setMorningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setMorningReminder(time)
            // Schedule the notification! 👈 THIS WAS MISSING
            scheduleMorningReminder(time)
        }
    }

    fun setEveningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setEveningReminder(time)
            // Schedule the notification! 👈 THIS WAS MISSING
            scheduleEveningReminder(time)
        }
    }

    private fun scheduleMorningReminder(time: String) {
        if (!_uiState.value.notificationsEnabled) return

        try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            notificationHelper.scheduleNotification(
                title = "☀️ Good Morning!",
                message = "Rise and shine! Time to crush your habits today!",
                hour = hour,
                minute = minute,
                notificationId = MORNING_NOTIFICATION_ID
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleEveningReminder(time: String) {
        if (!_uiState.value.notificationsEnabled) return

        try {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            notificationHelper.scheduleNotification(
                title = "🌙 Evening Check-in",
                message = "End your day strong! Complete your remaining habits.",
                hour = hour,
                minute = minute,
                notificationId = EVENING_NOTIFICATION_ID
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleAllReminders() {
        scheduleMorningReminder(_uiState.value.morningReminder)
        scheduleEveningReminder(_uiState.value.eveningReminder)
    }

    private fun cancelAllReminders() {
        notificationHelper.cancelNotification(MORNING_NOTIFICATION_ID)
        notificationHelper.cancelNotification(EVENING_NOTIFICATION_ID)
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesManager.updateUserName(name)
        }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.update { it.copy(showLogoutDialog = show) }
    }

    fun showClearDataDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDataDialog = show) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            cancelAllReminders()
            preferencesManager.logout()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showLogoutDialog = false,
                    logoutSuccess = true
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            habitRepository.deleteAllHabits()
            habitRepository.deleteAllHabitLogs()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showClearDataDialog = false
                )
            }
        }
    }
}