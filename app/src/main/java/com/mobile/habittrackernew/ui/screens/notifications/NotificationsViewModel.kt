package com.mobile.habittrackernew.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.NotificationHelper
import com.mobile.habittrackernew.services.ScheduledNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val hasNotificationPermission: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val scheduledNotifications: List<ScheduledNotification> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        clearNotificationCount() // Clear count when screen is opened
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val hasPermission = notificationHelper.hasNotificationPermission()
            val notificationsEnabled = notificationHelper.areNotificationsEnabled()
            val notifications = notificationHelper.getScheduledNotifications()

            _uiState.update {
                it.copy(
                    hasNotificationPermission = hasPermission,
                    notificationsEnabled = notificationsEnabled,
                    scheduledNotifications = notifications,
                    isLoading = false
                )
            }
        }
    }

    private fun clearNotificationCount() {
        viewModelScope.launch {
            preferencesManager.clearNotificationCount()
        }
    }

    fun toggleNotification(notificationId: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedNotifications = state.scheduledNotifications.map { notification ->
                    if (notification.id == notificationId) {
                        val newEnabled = !notification.isEnabled

                        // Actually schedule or cancel the notification
                        if (newEnabled) {
                            val (hour, minute) = parseTime(notification.time)
                            notificationHelper.scheduleNotification(
                                title = notification.title,
                                message = notification.message,
                                hour = hour,
                                minute = minute,
                                notificationId = notification.id
                            )
                        } else {
                            notificationHelper.cancelNotification(notification.id)
                        }

                        notification.copy(isEnabled = newEnabled)
                    } else {
                        notification
                    }
                }
                state.copy(scheduledNotifications = updatedNotifications)
            }
        }
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        return try {
            val parts = time.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    fun sendTestNotification() {
        notificationHelper.showInstantNotification(
            title = "Test Notification 🔔",
            message = "Great! Notifications are working perfectly! Keep building those habits! 💪"
        )
    }

    fun requestNotificationPermission() {
        // This will be handled by the activity
        // For now, just refresh the state
        loadNotifications()
    }

    fun refresh() {
        loadNotifications()
    }
}