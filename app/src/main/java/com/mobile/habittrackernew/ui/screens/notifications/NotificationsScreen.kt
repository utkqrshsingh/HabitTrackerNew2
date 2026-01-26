package com.mobile.habittrackernew.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mobile.habittrackernew.services.NotificationType
import com.mobile.habittrackernew.services.ScheduledNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh when screen becomes visible (e.g., returning from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Status Card
            item {
                PermissionStatusCard(
                    hasPermission = uiState.hasNotificationPermission,
                    notificationsEnabled = uiState.notificationsEnabled,
                    onRequestPermission = { viewModel.requestNotificationPermission() }
                )
            }

            // Master Toggle Info (if notifications are disabled in settings)
            if (!uiState.notificationsEnabled) {
                item {
                    NotificationsDisabledCard()
                }
            }

            // Quick Actions
            item {
                QuickActionsCard(
                    onSendTestNotification = { viewModel.sendTestNotification() },
                    enabled = uiState.hasNotificationPermission && uiState.notificationsEnabled
                )
            }

            // Scheduled Notifications Header
            item {
                Text(
                    text = "Scheduled Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Notification List
            items(uiState.scheduledNotifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onToggle = { viewModel.toggleNotification(notification.id) },
                    enabled = uiState.notificationsEnabled
                )
            }

            // Tips Section
            item {
                TipsCard()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun PermissionStatusCard(
    hasPermission: Boolean,
    notificationsEnabled: Boolean,
    onRequestPermission: () -> Unit
) {
    val isFullyEnabled = hasPermission && notificationsEnabled

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyEnabled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFullyEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFullyEnabled)
                        Icons.Default.NotificationsActive
                    else
                        Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        !hasPermission -> "Permission Required"
                        !notificationsEnabled -> "Notifications Disabled"
                        else -> "Notifications Enabled"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFullyEnabled)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = when {
                        !hasPermission -> "Grant permission to receive reminders"
                        !notificationsEnabled -> "Enable in Settings to receive reminders"
                        else -> "You'll receive habit reminders"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFullyEnabled)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }

            if (!hasPermission) {
                Button(onClick = onRequestPermission) {
                    Text("Enable")
                }
            }
        }
    }
}

@Composable
fun NotificationsDisabledCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Notifications are turned off in Settings. Go to Settings > Notifications to enable them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsCard(
    onSendTestNotification: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSendTestNotification,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Test Notification")
            }

            if (!enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enable notifications to test",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: ScheduledNotification,
    onToggle: () -> Unit,
    enabled: Boolean = true
) {
    val isActuallyEnabled = notification.isEnabled && enabled

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActuallyEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        getNotificationColor(notification.type).copy(
                            alpha = if (isActuallyEnabled) 0.2f else 0.1f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = getNotificationColor(notification.type).copy(
                        alpha = if (isActuallyEnabled) 1f else 0.5f
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isActuallyEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isActuallyEnabled) 1f else 0.5f
                    ),
                    maxLines = 1
                )
                Text(
                    text = "⏰ ${notification.time}",
                    style = MaterialTheme.typography.labelMedium,
                    color = getNotificationColor(notification.type).copy(
                        alpha = if (isActuallyEnabled) 1f else 0.5f
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Switch(
                checked = notification.isEnabled,
                onCheckedChange = { onToggle() },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = getNotificationColor(notification.type),
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
                    disabledCheckedTrackColor = getNotificationColor(notification.type).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "💡 Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Set reminders at times when you're most likely to complete habits\n" +
                        "• Morning reminders help start your day right\n" +
                        "• Evening reminders help you reflect on progress\n" +
                        "• You can customize reminder times in Settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.MORNING_REMINDER -> Color(0xFFF59E0B)
        NotificationType.EVENING_REMINDER -> Color(0xFF8B5CF6)
        NotificationType.HABIT_REMINDER -> Color(0xFF10B981)
        NotificationType.STREAK_ALERT -> Color(0xFFEF4444)
        NotificationType.MOTIVATION -> Color(0xFF3B82F6)
    }
}