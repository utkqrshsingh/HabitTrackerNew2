package com.mobile.habittrackernew.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val scheduledNotifications: List<ScheduledNotification> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val hasPermission = notificationHelper.hasNotificationPermission()
            val notifications = notificationHelper.getScheduledNotifications()

            _uiState.update {
                it.copy(
                    hasNotificationPermission = hasPermission,
                    scheduledNotifications = notifications,
                    isLoading = false
                )
            }
        }
    }

    fun toggleNotification(notificationId: Int) {
        _uiState.update { state ->
            val updatedNotifications = state.scheduledNotifications.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isEnabled = !notification.isEnabled)
                } else {
                    notification
                }
            }
            state.copy(scheduledNotifications = updatedNotifications)
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
}