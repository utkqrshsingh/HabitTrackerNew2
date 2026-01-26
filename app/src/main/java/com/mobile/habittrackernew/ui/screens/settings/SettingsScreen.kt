package com.mobile.habittrackernew.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.habittrackernew.ui.theme.GradientPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(GradientPurple)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.userName.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userName.ifEmpty { "User" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.userEmail.ifEmpty { "user@example.com" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            editedName = uiState.userName
                            showEditNameDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences Section
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            SettingsCard {
                // Dark Mode
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    isChecked = uiState.isDarkMode,
                    onToggle = { viewModel.toggleDarkMode() }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Notifications
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Enable push notifications",
                    isChecked = uiState.notificationsEnabled,
                    onToggle = { viewModel.toggleNotifications() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminders Section
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Default.Schedule,
                    title = "Morning Reminder",
                    subtitle = uiState.morningReminder,
                    onClick = { /* Show time picker */ }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickItem(
                    icon = Icons.Default.Schedule,
                    title = "Evening Reminder",
                    subtitle = uiState.eveningReminder,
                    onClick = { /* Show time picker */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General Section
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Invite friends to build habits",
                    onClick = { /* Share app */ }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickItem(
                    icon = Icons.Default.Star,
                    title = "Rate Us",
                    subtitle = "Love the app? Rate us on Play Store",
                    onClick = { /* Open Play Store */ }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { /* Show about dialog */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            SettingsCard {
                SettingsClickItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all habits and progress",
                    onClick = { viewModel.showClearDataDialog(true) },
                    tint = MaterialTheme.colorScheme.error
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsClickItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    onClick = { viewModel.showLogoutDialog(true) },
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserName(editedName)
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutDialog(false) },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout() }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLogoutDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data Dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearDataDialog(false) },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete all your habits, streaks, and progress. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearDataDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (tint == MaterialTheme.colorScheme.error)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = tint
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}