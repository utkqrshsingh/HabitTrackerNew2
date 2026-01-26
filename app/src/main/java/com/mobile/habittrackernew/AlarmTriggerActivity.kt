package com.mobile.habittrackernew

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mobile.habittrackernew.data.preferences.PreferencesManager
import com.mobile.habittrackernew.services.AlarmService
import com.mobile.habittrackernew.ui.screens.alarms.AlarmTriggerScreen
import com.mobile.habittrackernew.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmTriggerActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // MUST set window flags BEFORE super.onCreate()
        setupWindowForLockScreen()

        super.onCreate(savedInstanceState)

        // Dismiss keyguard
        dismissKeyguard()

        val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID,
            intent.getLongExtra("alarm_id", 0))
        val label = intent.getStringExtra(AlarmService.EXTRA_ALARM_LABEL)
            ?: intent.getStringExtra("alarm_label")
            ?: "Alarm"
        val isSnooze = intent.getBooleanExtra(AlarmService.EXTRA_IS_SNOOZE,
            intent.getBooleanExtra("is_snooze", false))

        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)

            HabitTrackerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmTriggerScreen(
                        alarmId = alarmId,
                        alarmLabel = label,
                        isSnooze = isSnooze,
                        onDismiss = {
                            stopAlarmAndFinish()
                        },
                        onSnooze = {
                            stopAlarmAndFinish()
                        }
                    )
                }
            }
        }
    }

    private fun setupWindowForLockScreen() {
        // These must be set BEFORE super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Window flags for showing over lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun dismissKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    // Keyguard dismissed
                }

                override fun onDismissCancelled() {
                    // User cancelled - still show alarm
                }

                override fun onDismissError() {
                    // Error - still show alarm
                }
            })
        }
    }

    private fun stopAlarmAndFinish() {
        try {
            val stopIntent = Intent(this, AlarmService::class.java).apply {
                action = AlarmService.ACTION_STOP_ALARM
            }
            startService(stopIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finishAndRemoveTask()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        if (AlarmService.isAlarmActive) {
            try {
                val stopIntent = Intent(this, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_STOP_ALARM
                }
                startService(stopIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Don't allow back button - user must tap Snooze or Dismiss
    }
}