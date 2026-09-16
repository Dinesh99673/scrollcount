package com.probuilder.scrollcount.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scrollcount_settings")

/**
 * Reads and writes the app's settings using DataStore.
 *
 * DataStore is used instead of SharedPreferences because it exposes settings as
 * a Flow, so the service and every screen react to a change immediately without
 * any listener plumbing.
 */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val TRACK_INSTAGRAM = booleanPreferencesKey("track_instagram")
        val TRACK_YOUTUBE = booleanPreferencesKey("track_youtube")
        val DAILY_LIMIT = intPreferencesKey("daily_limit")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val SECONDS_PER_REEL = intPreferencesKey("seconds_per_reel")
        val DEBUG_LOGGING = booleanPreferencesKey("debug_logging")

        val NOTIFIED_DAY = longPreferencesKey("notified_day")
        val NOTIFIED_HALF = booleanPreferencesKey("notified_half")
        val NOTIFIED_FULL = booleanPreferencesKey("notified_full")
        val SUMMARY_SENT_DAY = longPreferencesKey("summary_sent_day")
    }

    /** A corrupted or missing file should never crash the app, so fall back to defaults. */
    private val preferences: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val settings: Flow<AppSettings> = preferences.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            trackInstagram = prefs[Keys.TRACK_INSTAGRAM] ?: defaults.trackInstagram,
            trackYouTube = prefs[Keys.TRACK_YOUTUBE] ?: defaults.trackYouTube,
            dailyLimit = prefs[Keys.DAILY_LIMIT] ?: defaults.dailyLimit,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: defaults.notificationsEnabled,
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: defaults.onboardingComplete,
            secondsPerReel = prefs[Keys.SECONDS_PER_REEL] ?: defaults.secondsPerReel,
            debugLogging = prefs[Keys.DEBUG_LOGGING] ?: defaults.debugLogging,
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setTrackInstagram(enabled: Boolean) = put(Keys.TRACK_INSTAGRAM, enabled)

    suspend fun setTrackYouTube(enabled: Boolean) = put(Keys.TRACK_YOUTUBE, enabled)

    suspend fun setTrackingFor(packageName: String, enabled: Boolean) {
        when (packageName) {
            com.probuilder.scrollcount.data.TrackedApps.INSTAGRAM -> setTrackInstagram(enabled)
            com.probuilder.scrollcount.data.TrackedApps.YOUTUBE -> setTrackYouTube(enabled)
        }
    }

    suspend fun setDailyLimit(limit: Int) = put(Keys.DAILY_LIMIT, limit.coerceIn(5, 1000))

    suspend fun setNotificationsEnabled(enabled: Boolean) = put(Keys.NOTIFICATIONS_ENABLED, enabled)

    suspend fun setOnboardingComplete(complete: Boolean) = put(Keys.ONBOARDING_COMPLETE, complete)

    suspend fun setDebugLogging(enabled: Boolean) = put(Keys.DEBUG_LOGGING, enabled)

    // --- limit-alert bookkeeping -------------------------------------------

    val notificationState: Flow<NotificationState> = preferences.map { prefs ->
        NotificationState(
            day = prefs[Keys.NOTIFIED_DAY] ?: 0L,
            halfNotified = prefs[Keys.NOTIFIED_HALF] ?: false,
            fullNotified = prefs[Keys.NOTIFIED_FULL] ?: false,
            summarySentOnDay = prefs[Keys.SUMMARY_SENT_DAY] ?: 0L,
        )
    }

    suspend fun notificationStateNow(): NotificationState = notificationState.first()

    /** Records that an alert was sent, resetting the flags when the day changes. */
    suspend fun markAlertSent(day: Long, half: Boolean, full: Boolean) {
        store.edit { prefs ->
            val sameDay = (prefs[Keys.NOTIFIED_DAY] ?: 0L) == day
            prefs[Keys.NOTIFIED_DAY] = day
            prefs[Keys.NOTIFIED_HALF] = half || (sameDay && prefs[Keys.NOTIFIED_HALF] == true)
            prefs[Keys.NOTIFIED_FULL] = full || (sameDay && prefs[Keys.NOTIFIED_FULL] == true)
        }
    }

    suspend fun markSummarySent(day: Long) = put(Keys.SUMMARY_SENT_DAY, day)

    /** Used by "Reset all data" so old alerts do not stay suppressed. */
    suspend fun clearAlertHistory() {
        store.edit { prefs ->
            prefs.remove(Keys.NOTIFIED_DAY)
            prefs.remove(Keys.NOTIFIED_HALF)
            prefs.remove(Keys.NOTIFIED_FULL)
            prefs.remove(Keys.SUMMARY_SENT_DAY)
        }
    }

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        store.edit { it[key] = value }
    }
}
