package com.probuilder.scrollcount.util

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Small helpers that turn raw numbers into the short, friendly strings the
 * screens show. Kept in one file so wording stays consistent everywhere.
 */
object Formatting {

    /** "1h 24m", "12m" or "48s" - whichever reads best for the amount given. */
    fun duration(totalSeconds: Int): String {
        if (totalSeconds < 60) return "${totalSeconds}s"
        val minutes = totalSeconds / 60
        if (minutes < 60) return "${minutes}m"
        val hours = minutes / 60
        val leftoverMinutes = minutes % 60
        return if (leftoverMinutes == 0) "${hours}h" else "${hours}h ${leftoverMinutes}m"
    }

    /** Estimated watch time for a number of reels, at [secondsPerReel] each. */
    fun estimatedSeconds(reelCount: Int, secondsPerReel: Int): Int = reelCount * secondsPerReel

    /** "Mon", "Tue"... used as bar chart labels. */
    fun shortDayName(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    /** "9 AM", "12 PM", "11 PM" - hour labels for the hourly chart. */
    fun hourLabel(hour: Int): String = when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }

    /** Percentage of the daily limit used, clamped to 0..999 so the UI never breaks. */
    fun percentOfLimit(count: Int, limit: Int): Int {
        if (limit <= 0) return 0
        return ((count.toFloat() / limit) * 100f).roundToInt().coerceIn(0, 999)
    }
}
