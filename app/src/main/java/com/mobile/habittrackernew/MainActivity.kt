package com.mobile.habittrackernew

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mobile.habittrackernew.services.DailyReminderWorker
import com.mobile.habittrackernew.services.MotivationWorker
import com.mobile.habittrackernew.ui.navigation.HabitTrackerNavHost
import com.mobile.habittrackernew.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        scheduleNotifications()

        setContent {
            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HabitTrackerNavHost()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleNotifications() {
        val workManager = WorkManager.getInstance(this)

        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(calculateInitialDelay(8, 0), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )

        val motivationRequest = PeriodicWorkRequestBuilder<MotivationWorker>(
            4, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "motivation_check",
            ExistingPeriodicWorkPolicy.KEEP,
            motivationRequest
        )
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}