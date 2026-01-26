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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.services.NotificationType
import com.mobile.habittrackernew.services.ScheduledNotification

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onRequestPermission = { viewModel.requestNotificationPermission() }
                )
            }

            // Quick Actions
            item {
                QuickActionsCard(
                    onSendTestNotification = { viewModel.sendTestNotification() }
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
                    onToggle = { viewModel.toggleNotification(notification.id) }
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
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPermission)
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
                        if (hasPermission)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasPermission)
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
                    text = if (hasPermission)
                        "Notifications Enabled"
                    else
                        "Notifications Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasPermission)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (hasPermission)
                        "You'll receive habit reminders"
                    else
                        "Enable notifications to get reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasPermission)
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
fun QuickActionsCard(
    onSendTestNotification: () -> Unit
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Test Notification")
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: ScheduledNotification,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isEnabled)
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
                    .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = getNotificationColor(notification.type)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "⏰ ${notification.time}",
                    style = MaterialTheme.typography.labelMedium,
                    color = getNotificationColor(notification.type),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Switch(
                checked = notification.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = getNotificationColor(notification.type)
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
                        "• Evening reminders help you reflect on progress",
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