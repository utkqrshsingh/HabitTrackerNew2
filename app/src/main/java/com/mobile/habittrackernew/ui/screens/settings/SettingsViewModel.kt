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
    val isLoading: Boolean = true,
    val logoutSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val userName = preferencesManager.userName.first()
            val userEmail = preferencesManager.userEmail.first()
            val isDarkMode = preferencesManager.isDarkMode.first()
            val notificationsEnabled = preferencesManager.notificationsEnabled.first()
            val morningReminder = preferencesManager.morningReminder.first()
            val eveningReminder = preferencesManager.eveningReminder.first()

            _uiState.update {
                it.copy(
                    userName = userName,
                    userEmail = userEmail,
                    isDarkMode = isDarkMode,
                    notificationsEnabled = notificationsEnabled,
                    morningReminder = morningReminder,
                    eveningReminder = eveningReminder,
                    isLoading = false
                )
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkMode
            preferencesManager.setDarkMode(newValue)
            _uiState.update { it.copy(isDarkMode = newValue) }
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationsEnabled
            preferencesManager.setNotificationsEnabled(newValue)
            _uiState.update { it.copy(notificationsEnabled = newValue) }
        }
    }

    fun setMorningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setMorningReminder(time)
            _uiState.update { it.copy(morningReminder = time) }
        }
    }

    fun setEveningReminder(time: String) {
        viewModelScope.launch {
            preferencesManager.setEveningReminder(time)
            _uiState.update { it.copy(eveningReminder = time) }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesManager.updateUserName(name)
            _uiState.update { it.copy(userName = name) }
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
            preferencesManager.logout()
            _uiState.update {
                it.copy(
                    showLogoutDialog = false,
                    logoutSuccess = true
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAIMessages()
            // Clear other data as needed
            _uiState.update { it.copy(showClearDataDialog = false) }
        }
    }
}