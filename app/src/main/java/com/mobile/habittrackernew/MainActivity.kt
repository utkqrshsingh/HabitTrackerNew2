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
import com.mobile.habittrackernew.services.PermissionManager
import com.mobile.habittrackernew.ui.navigation.HabitTrackerNavHost
import com.mobile.habittrackernew.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var permissionManager: PermissionManager

    // Track if we've already requested overlay permission this session
    private var hasRequestedOverlay = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Continue with other permissions
            requestBatteryOptimization()
        } else {
            // Still try to request other permissions
            requestBatteryOptimization()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        requestPermissionsIfNeeded()

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
        // Check overlay permission when user comes back from settings
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

        // Step 3: Overlay permission (Display over other apps) - CRITICAL!
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
            // Small delay to avoid multiple dialogs at once
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
        // If user granted overlay permission, continue with other permissions
        if (hasRequestedOverlay && permissionManager.hasOverlayPermission()) {
            // Check if we need exact alarm permission
            if (!permissionManager.hasExactAlarmPermission()) {
                requestExactAlarmPermission()
            }
        }
    }
}