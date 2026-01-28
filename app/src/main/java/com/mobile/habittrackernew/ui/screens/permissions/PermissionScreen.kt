package com.mobile.habittrackernew.ui.screens.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mobile.habittrackernew.services.PermissionHelper
import com.mobile.habittrackernew.services.PermissionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit,
    onSkip: () -> Unit = onAllPermissionsGranted
) {
    val context = LocalContext.current
    val activity = context as Activity
    val permissionHelper = remember { PermissionHelper(context) }
    val scope = rememberCoroutineScope()

    var permissionStatus by remember { mutableStateOf(permissionHelper.checkAllPermissions()) }
    var isRequesting by remember { mutableStateOf(false) }

    // Refresh permissions when screen resumes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = permissionHelper.checkAllPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-navigate when all granted
    LaunchedEffect(permissionStatus.allGranted) {
        if (permissionStatus.allGranted) {
            delay(500) // Small delay for visual feedback
            onAllPermissionsGranted()
        }
    }

    // Notification permission launcher (Android 13+)
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionStatus = permissionHelper.checkAllPermissions()
    }

    // Multiple permissions launcher
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionStatus = permissionHelper.checkAllPermissions()
    }

    // Function to request all permissions automatically
    fun requestAllPermissions() {
        isRequesting = true
        scope.launch {
            // Step 1: Request notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !permissionStatus.hasNotificationPermission) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                delay(500)
            }

            // Step 2: Request battery optimization exemption
            if (!permissionStatus.hasBatteryOptimizationExempt) {
                permissionHelper.requestBatteryOptimization(activity)
                delay(300)
            }

            isRequesting = false
            permissionStatus = permissionHelper.checkAllPermissions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Header
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enable Alarm Permissions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "For alarms to work when your phone is locked or the app is closed, please grant these permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ===== PERMISSION ITEMS =====

        // 1. Notifications
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Show alarm alerts",
            isGranted = permissionStatus.hasNotificationPermission,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionHelper.openNotificationSettings(activity)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Exact Alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionItem(
                icon = Icons.Default.Alarm,
                title = "Exact Alarms",
                description = "Ring at exact times",
                isGranted = permissionStatus.hasExactAlarmPermission,
                onRequest = {
                    permissionHelper.openExactAlarmSettings(activity)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. Display Over Other Apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionItem(
                icon = Icons.Default.Fullscreen,
                title = "Display Over Apps",
                description = "Show alarm on lock screen",
                isGranted = permissionStatus.hasOverlayPermission,
                onRequest = {
                    permissionHelper.openOverlaySettings(activity)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Battery Optimization
        PermissionItem(
            icon = Icons.Default.BatteryChargingFull,
            title = "Battery Optimization",
            description = "Keep alarms running",
            isGranted = permissionStatus.hasBatteryOptimizationExempt,
            onRequest = {
                permissionHelper.openBatterySettings(activity)
            }
        )

        // 5. Full Screen Intent (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Spacer(modifier = Modifier.height(12.dp))
            PermissionItem(
                icon = Icons.Default.PhoneAndroid,
                title = "Full Screen Alerts",
                description = "Show full screen alarm",
                isGranted = permissionStatus.hasFullScreenIntentPermission,
                onRequest = {
                    permissionHelper.openFullScreenSettings(activity)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress
        val actualTotal = getActualPermissionCount()
        val grantedCount = getGrantedCount(permissionStatus)

        LinearProgressIndicator(
            progress = grantedCount.toFloat() / actualTotal.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (permissionStatus.allGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$grantedCount of $actualTotal permissions granted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grant All Button
        if (!permissionStatus.allGranted) {
            Button(
                onClick = { requestAllPermissions() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isRequesting
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Requesting...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant All Permissions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // All Granted - Continue Button
            Button(
                onClick = onAllPermissionsGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("All Set! Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip
        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for now",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun getActualPermissionCount(): Int {
    var count = 2 // Notifications + Battery (always needed)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) count++ // Exact alarms
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) count++ // Overlay
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) count++ // Full screen
    return count
}

@Composable
private fun getGrantedCount(status: PermissionStatus): Int {
    var count = 0
    if (status.hasNotificationPermission) count++
    if (status.hasBatteryOptimizationExempt) count++
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && status.hasExactAlarmPermission) count++
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && status.hasOverlayPermission) count++
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && status.hasFullScreenIntentPermission) count++
    return count
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
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
                    .clip(CircleShape)
                    .background(
                        if (isGranted)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp)
                )
            } else {
                FilledTonalButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Allow", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
