package com.mobile.habittrackernew

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.NotificationHelper
import com.mobile.habittrackernew.services.PermissionManager
import com.mobile.habittrackernew.ui.navigation.HabitTrackerNavHost
import com.mobile.habittrackernew.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private lateinit var permissionManager: PermissionManager

    private var hasRequestedOverlay = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestBatteryOptimization()
        } else {
            requestBatteryOptimization()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        requestPermissionsIfNeeded()

        // Schedule reminders on app start
        scheduleRemindersIfEnabled()

        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)

            HabitTrackerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerNavHost()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkOverlayPermissionOnResume()
    }

    private fun requestPermissionsIfNeeded() {
        // Step 1: Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionManager.hasNotificationPermission()) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Step 2: Battery optimization
        if (!permissionManager.hasBatteryOptimizationExempt()) {
            requestBatteryOptimization()
            return
        }

        // Step 3: Overlay permission (Display over other apps)
        if (!permissionManager.hasOverlayPermission() && !hasRequestedOverlay) {
            requestOverlayPermission()
            return
        }

        // Step 4: Exact alarm permission (Android 12+)
        if (!permissionManager.hasExactAlarmPermission()) {
            requestExactAlarmPermission()
        }
    }

    private fun requestBatteryOptimization() {
        if (!permissionManager.hasBatteryOptimizationExempt()) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // After battery, request overlay
        if (!permissionManager.hasOverlayPermission() && !hasRequestedOverlay) {
            window.decorView.postDelayed({
                requestOverlayPermission()
            }, 500)
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                hasRequestedOverlay = true
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!permissionManager.hasExactAlarmPermission()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkOverlayPermissionOnResume() {
        if (hasRequestedOverlay && permissionManager.hasOverlayPermission()) {
            if (!permissionManager.hasExactAlarmPermission()) {
                requestExactAlarmPermission()
            }
        }
    }

    private fun scheduleRemindersIfEnabled() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = preferencesManager.notificationsEnabled.first()
                if (notificationsEnabled) {
                    val morningTime = preferencesManager.morningReminder.first()
                    val eveningTime = preferencesManager.eveningReminder.first()

                    scheduleMorningReminder(morningTime)
                    scheduleEveningReminder(eveningTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleMorningReminder(time: String) {
        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                notificationHelper.scheduleNotification(
                    title = "☀️ Good Morning!",
                    message = "Rise and shine! Time to crush your habits today!",
                    hour = hour,
                    minute = minute,
                    notificationId = 1001
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleEveningReminder(time: String) {
        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                notificationHelper.scheduleNotification(
                    title = "🌙 Evening Check-in",
                    message = "End your day strong! Complete your remaining habits.",
                    hour = hour,
                    minute = minute,
                    notificationId = 1003
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}