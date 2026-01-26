package com.mobile.habittrackernew.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Collect all preferences
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
            // UI will update automatically through the flow
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationsEnabled
            preferencesManager.setNotificationsEnabled(newValue)
        }
    }

    fun setMorningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setMorningReminder(time)
        }
    }

    fun setEveningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setEveningReminder(time)
        }
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