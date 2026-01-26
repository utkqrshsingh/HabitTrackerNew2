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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

data class PermissionStatus(
    val hasNotificationPermission: Boolean = false,
    val hasExactAlarmPermission: Boolean = false,
    val hasBatteryOptimizationExempt: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasFullScreenIntentPermission: Boolean = false
) {
    val allGranted: Boolean
        get() {
            val notificationGranted = hasNotificationPermission
            val batteryGranted = hasBatteryOptimizationExempt
            val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasExactAlarmPermission else true
            val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) hasOverlayPermission else true
            val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) hasFullScreenIntentPermission else true
            
            return notificationGranted && batteryGranted && exactAlarmGranted && overlayGranted && fullScreenGranted
        }
}

class PermissionHelper(private val context: Context) {

    fun checkAllPermissions(): PermissionStatus {
        return PermissionStatus(
            hasNotificationPermission = hasNotificationPermission(),
            hasExactAlarmPermission = hasExactAlarmPermission(),
            hasBatteryOptimizationExempt = hasBatteryOptimizationExempt(),
            hasOverlayPermission = hasOverlayPermission(),
            hasFullScreenIntentPermission = hasFullScreenIntentPermission()
        )
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun hasBatteryOptimizationExempt(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun hasFullScreenIntentPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                nm.canUseFullScreenIntent()
            } else {
                true
            }
        } else {
            true
        }
    }

    fun requestBatteryOptimization(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            openBatterySettings(activity)
        }
    }

    fun openNotificationSettings(activity: Activity) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
            }
        }
        activity.startActivity(intent)
    }

    fun openExactAlarmSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        }
    }

    fun openOverlaySettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        }
    }

    fun openBatterySettings(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    fun openFullScreenSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
        }
    }
}
