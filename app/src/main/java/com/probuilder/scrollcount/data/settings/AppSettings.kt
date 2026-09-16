package com.probuilder.scrollcount.data.settings

import com.probuilder.scrollcount.data.TrackedApps

/**
 * Every user-changeable setting, in one immutable snapshot.
 *
 * The accessibility service keeps the latest copy of this in a field so it can
 * decide whether to count a reel without doing any suspending work on the main
 * thread.
 */
data class AppSettings(
    val trackInstagram: Boolean = true,
    val trackYouTube: Boolean = true,
    val dailyLimit: Int = 100,
    val notificationsEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
    /** Used only for the "estimated time spent" figure on the home screen. */
    val secondsPerReel: Int = 20,
    /** Dumps the on-screen view tree to Logcat. Debug builds only. */
    val debugLogging: Boolean = false,
) {
    /** True if reels from this app should be counted right now. */
    fun isTracked(packageName: String): Boolean = when (packageName) {
        TrackedApps.INSTAGRAM -> trackInstagram
        TrackedApps.YOUTUBE -> trackYouTube
        else -> false
    }
}

/**
 * Remembers which limit alerts have already been sent today, so the app warns
 * once at 50% and once at 100% rather than on every single swipe.
 */
data class NotificationState(
    /** Day the flags below belong to, as an epoch day number. */
    val day: Long = 0L,
    val halfNotified: Boolean = false,
    val fullNotified: Boolean = false,
    val summarySentOnDay: Long = 0L,
)
