package com.example.scrollsense.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scrollsense_preferences")

data class UserPreferences(
    val userName: String = "Aman",
    val isOnboardingCompleted: Boolean = false,
    val instagramLimitMinutes: Int = 30,
    val instagramVideoTarget: Int = 50,
    val instagramEnabled: Boolean = true,
    val youtubeLimitMinutes: Int = 45,
    val youtubeVideoTarget: Int = 50,
    val youtubeEnabled: Boolean = true,
    val unifiedShortVideosLimit: Int = 50,
    val unifiedShortVideosCount: Int = 0,
    val unifiedShortVideosDateMillis: Long = 0L,
    val resetHour: Int = 0,
    val warningThresholdPercent: Int = 80,
    val isTestModeEnabled: Boolean = false,
    val testLimitMinutes: Int = 3,
    val isDemoModeActive: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val lastWarningNotificationDate: String = "",
    val themeMode: ThemeMode = ThemeMode.DARK
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val INSTAGRAM_LIMIT_MINUTES = intPreferencesKey("instagram_limit_minutes")
        val INSTAGRAM_VIDEO_TARGET = intPreferencesKey("instagram_video_target")
        val INSTAGRAM_ENABLED = booleanPreferencesKey("instagram_enabled")
        val YOUTUBE_LIMIT_MINUTES = intPreferencesKey("youtube_limit_minutes")
        val YOUTUBE_VIDEO_TARGET = intPreferencesKey("youtube_video_target")
        val YOUTUBE_ENABLED = booleanPreferencesKey("youtube_enabled")
        val UNIFIED_SHORT_VIDEOS_LIMIT = intPreferencesKey("unified_short_videos_limit")
        val UNIFIED_SHORT_VIDEOS_COUNT = intPreferencesKey("unified_short_videos_count")
        val UNIFIED_SHORT_VIDEOS_DATE_MILLIS = longPreferencesKey("unified_short_videos_date_millis")
        val RESET_HOUR = intPreferencesKey("reset_hour")
        val WARNING_THRESHOLD_PERCENT = intPreferencesKey("warning_threshold_percent")
        val TEST_MODE_ENABLED = booleanPreferencesKey("test_mode_enabled")
        val TEST_LIMIT_MINUTES = intPreferencesKey("test_limit_minutes")
        val DEMO_MODE_ACTIVE = booleanPreferencesKey("demo_mode_active")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LAST_WARNING_NOTIFICATION_DATE = stringPreferencesKey("last_warning_notification_date")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        val parsedThemeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.DARK
        }

        UserPreferences(
            userName = preferences[PreferencesKeys.USER_NAME] ?: "Aman",
            isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            instagramLimitMinutes = preferences[PreferencesKeys.INSTAGRAM_LIMIT_MINUTES] ?: 30,
            instagramVideoTarget = preferences[PreferencesKeys.INSTAGRAM_VIDEO_TARGET] ?: 50,
            instagramEnabled = preferences[PreferencesKeys.INSTAGRAM_ENABLED] ?: true,
            youtubeLimitMinutes = preferences[PreferencesKeys.YOUTUBE_LIMIT_MINUTES] ?: 45,
            youtubeVideoTarget = preferences[PreferencesKeys.YOUTUBE_VIDEO_TARGET] ?: 50,
            youtubeEnabled = preferences[PreferencesKeys.YOUTUBE_ENABLED] ?: true,
            unifiedShortVideosLimit = preferences[PreferencesKeys.UNIFIED_SHORT_VIDEOS_LIMIT] ?: 50,
            unifiedShortVideosCount = preferences[PreferencesKeys.UNIFIED_SHORT_VIDEOS_COUNT] ?: 0,
            unifiedShortVideosDateMillis = preferences[PreferencesKeys.UNIFIED_SHORT_VIDEOS_DATE_MILLIS] ?: 0L,
            resetHour = preferences[PreferencesKeys.RESET_HOUR] ?: 0,
            warningThresholdPercent = preferences[PreferencesKeys.WARNING_THRESHOLD_PERCENT] ?: 80,
            isTestModeEnabled = preferences[PreferencesKeys.TEST_MODE_ENABLED] ?: false,
            testLimitMinutes = preferences[PreferencesKeys.TEST_LIMIT_MINUTES] ?: 3,
            isDemoModeActive = preferences[PreferencesKeys.DEMO_MODE_ACTIVE] ?: false,
            notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            lastWarningNotificationDate = preferences[PreferencesKeys.LAST_WARNING_NOTIFICATION_DATE] ?: "",
            themeMode = parsedThemeMode
        )
    }

    suspend fun getUnifiedDailyShortsCount(todayDateMillis: Long): Int {
        val prefs = context.dataStore.data.first()
        val savedDate = prefs[PreferencesKeys.UNIFIED_SHORT_VIDEOS_DATE_MILLIS] ?: 0L
        return if (savedDate == todayDateMillis) {
            prefs[PreferencesKeys.UNIFIED_SHORT_VIDEOS_COUNT] ?: 0
        } else {
            0
        }
    }

    suspend fun setUnifiedDailyShortsCount(todayDateMillis: Long, count: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.UNIFIED_SHORT_VIDEOS_DATE_MILLIS] = todayDateMillis
            prefs[PreferencesKeys.UNIFIED_SHORT_VIDEOS_COUNT] = count
        }
    }

    suspend fun setUnifiedShortVideosLimit(limit: Int) {
        context.dataStore.edit { it[PreferencesKeys.UNIFIED_SHORT_VIDEOS_LIMIT] = limit }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setAppLimit(app: ProtectedApp, limitMinutes: Int, videoTarget: Int, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (app) {
                ProtectedApp.INSTAGRAM -> {
                    prefs[PreferencesKeys.INSTAGRAM_LIMIT_MINUTES] = limitMinutes
                    prefs[PreferencesKeys.INSTAGRAM_VIDEO_TARGET] = videoTarget
                    prefs[PreferencesKeys.INSTAGRAM_ENABLED] = enabled
                }
                ProtectedApp.YOUTUBE -> {
                    prefs[PreferencesKeys.YOUTUBE_LIMIT_MINUTES] = limitMinutes
                    prefs[PreferencesKeys.YOUTUBE_VIDEO_TARGET] = videoTarget
                    prefs[PreferencesKeys.YOUTUBE_ENABLED] = enabled
                }
            }
        }
    }

    suspend fun setResetHour(hour: Int) {
        context.dataStore.edit { it[PreferencesKeys.RESET_HOUR] = hour }
    }

    suspend fun setWarningThreshold(percent: Int) {
        context.dataStore.edit { it[PreferencesKeys.WARNING_THRESHOLD_PERCENT] = percent }
    }

    suspend fun setTestModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TEST_MODE_ENABLED] = enabled }
    }

    suspend fun setTestLimitMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.TEST_LIMIT_MINUTES] = minutes }
    }

    suspend fun setDemoModeActive(active: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEMO_MODE_ACTIVE] = active }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setLastWarningNotificationDate(dateKey: String) {
        context.dataStore.edit { it[PreferencesKeys.LAST_WARNING_NOTIFICATION_DATE] = dateKey }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }
}
