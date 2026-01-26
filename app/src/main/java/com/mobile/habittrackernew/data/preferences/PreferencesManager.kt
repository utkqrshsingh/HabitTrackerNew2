package com.mobile.habittrackernew.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habit_tracker_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PHOTO = stringPreferencesKey("user_photo")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val REMINDER_TIME = stringPreferencesKey("reminder_time")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val DAILY_GOAL = stringPreferencesKey("daily_goal")
        private val MORNING_REMINDER = stringPreferencesKey("morning_reminder")
        private val EVENING_REMINDER = stringPreferencesKey("evening_reminder")
        private val NOTIFICATION_COUNT = intPreferencesKey("notification_count")
        private val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
        private val HAS_SEEDED_HABITS = booleanPreferencesKey("has_seeded_habits") // NEW
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE] ?: false
    }

    val useSystemTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_SYSTEM_THEME] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_FIRST_LAUNCH] ?: true
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL] ?: ""
    }

    val morningReminder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[MORNING_REMINDER] ?: "08:00"
    }

    val eveningReminder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EVENING_REMINDER] ?: "20:00"
    }

    val notificationCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_COUNT] ?: 0
    }

    // NEW: Check if habits have been seeded
    val hasSeededHabits: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_SEEDED_HABITS] ?: false
    }

    suspend fun setLoggedIn(
        isLoggedIn: Boolean,
        userId: String = "",
        userName: String = "",
        userEmail: String = "",
        userPhoto: String = ""
    ) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = isLoggedIn
            prefs[USER_ID] = userId
            prefs[USER_NAME] = userName
            prefs[USER_EMAIL] = userEmail
            prefs[USER_PHOTO] = userPhoto
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = isDark
            prefs[USE_SYSTEM_THEME] = false
        }
    }

    suspend fun setUseSystemTheme(useSystem: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USE_SYSTEM_THEME] = useSystem
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setFirstLaunch(isFirst: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_FIRST_LAUNCH] = isFirst
        }
    }

    suspend fun setMorningReminder(time: String) {
        context.dataStore.edit { prefs ->
            prefs[MORNING_REMINDER] = time
        }
    }

    suspend fun setEveningReminder(time: String) {
        context.dataStore.edit { prefs ->
            prefs[EVENING_REMINDER] = time
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }

    suspend fun setNotificationCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_COUNT] = count
        }
    }

    suspend fun incrementNotificationCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[NOTIFICATION_COUNT] ?: 0
            prefs[NOTIFICATION_COUNT] = current + 1
        }
    }

    suspend fun clearNotificationCount() {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_COUNT] = 0
        }
    }

    // NEW: Mark habits as seeded
    suspend fun setHasSeededHabits(seeded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_SEEDED_HABITS] = seeded
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
            prefs[USER_ID] = ""
            prefs[USER_NAME] = ""
            prefs[USER_EMAIL] = ""
            prefs[USER_PHOTO] = ""
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}