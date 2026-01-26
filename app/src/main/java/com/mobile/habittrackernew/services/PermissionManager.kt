package com.mobile.habittrackernew.services

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    companion object {
        const val REQUEST_CODE_NOTIFICATIONS = 1001
        const val REQUEST_CODE_OVERLAY = 1002
    }

    // ============ CHECK PERMISSIONS ============

    fun hasNotificationPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        } catch (e: Exception) {
            true
        }
    }

    fun hasExactAlarmPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    fun hasBatteryOptimizationExempt(): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            true
        }
    }

    // Check "Display over other apps" permission - CRITICAL for alarm popup
    fun hasOverlayPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    // Check Full Screen Intent permission (Android 14+)
    fun hasFullScreenIntentPermission(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.canUseFullScreenIntent()
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }

    // Minimum permissions for basic alarms
    fun hasMinimumPermissions(): Boolean {
        val hasNotification = hasNotificationPermission()
        val hasBattery = hasBatteryOptimizationExempt()

        android.util.Log.d("PermissionManager", "Notification: $hasNotification, Battery: $hasBattery")

        return hasNotification && hasBattery
    }

    // All permissions needed for alarm to show over lock screen
    fun hasAllAlarmPermissions(): Boolean {
        val hasNotification = hasNotificationPermission()
        val hasBattery = hasBatteryOptimizationExempt()
        val hasOverlay = hasOverlayPermission()
        val hasExactAlarm = hasExactAlarmPermission()
        val hasFullScreen = hasFullScreenIntentPermission()

        android.util.Log.d("PermissionManager",
            "Notification: $hasNotification, Battery: $hasBattery, Overlay: $hasOverlay, " +
                    "ExactAlarm: $hasExactAlarm, FullScreen: $hasFullScreen"
        )

        return hasNotification && hasBattery && hasOverlay && hasExactAlarm && hasFullScreen
    }

    // ============ REQUEST PERMISSIONS ============

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATIONS
            )
        }
    }

    // Request "Display over other apps" - Opens settings page
    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to app settings
                    openAppSettings(activity)
                }
            }
        }
    }

    // Request battery optimization exemption - Shows popup dialog
    fun requestBatteryOptimization(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                activity.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    // Request exact alarm permission (Android 12+)
    fun requestExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                openAppSettings(activity)
            }
        }
    }

    // Request full screen intent permission (Android 14+)
    fun requestFullScreenIntentPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                openAppSettings(activity)
            }
        }
    }

    // Open app settings as fallback
    fun openAppSettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============ REQUEST ALL PERMISSIONS AT ONCE ============

    fun requestAllAlarmPermissions(activity: Activity) {
        // 1. Notification permission (shows popup on Android 13+)
        if (!hasNotificationPermission()) {
            requestNotificationPermission(activity)
        }

        // 2. Battery optimization (shows popup)
        if (!hasBatteryOptimizationExempt()) {
            requestBatteryOptimization(activity)
        }

        // 3. Overlay permission (opens settings)
        if (!hasOverlayPermission()) {
            requestOverlayPermission(activity)
        }

        // 4. Exact alarm (Android 12+)
        if (!hasExactAlarmPermission()) {
            requestExactAlarmPermission(activity)
        }

        // 5. Full screen intent (Android 14+)
        if (!hasFullScreenIntentPermission()) {
            requestFullScreenIntentPermission(activity)
        }
    }
}