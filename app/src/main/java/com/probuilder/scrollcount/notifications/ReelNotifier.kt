package com.probuilder.scrollcount.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.probuilder.scrollcount.MainActivity
import com.probuilder.scrollcount.R
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.settings.SettingsRepository
import com.probuilder.scrollcount.util.TimeRanges
import kotlin.math.abs

/**
 * Builds and sends every notification the app can show.
 *
 * Two rules keep this from being annoying, and both are enforced here rather
 * than at the call sites: nothing is sent if the user turned notifications off,
 * and each daily-limit alert fires at most once per day.
 */
class ReelNotifier(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val reelRepository: ReelRepository,
) {

    private val manager = NotificationManagerCompat.from(context)

    /** Safe to call as often as you like; creating an existing channel is a no-op. */
    fun ensureChannels() {
        val limits = NotificationChannel(
            CHANNEL_LIMITS,
            context.getString(R.string.channel_limits_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.channel_limits_description) }

        val summary = NotificationChannel(
            CHANNEL_SUMMARY,
            context.getString(R.string.channel_summary_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = context.getString(R.string.channel_summary_description) }

        manager.createNotificationChannel(limits)
        manager.createNotificationChannel(summary)
    }

    /**
     * Called after every counted reel. Checks today's total against the limit
     * and sends the halfway or limit-reached alert if it is due.
     */
    suspend fun onReelCounted() {
        val settings = settingsRepository.current()
        if (!settings.notificationsEnabled || !canPost()) return
        val limit = settings.dailyLimit
        if (limit <= 0) return

        val count = reelRepository.todayCount()
        val today = TimeRanges.today().toEpochDay()
        val state = settingsRepository.notificationStateNow()
        val sameDay = state.day == today
        val halfAlreadySent = sameDay && state.halfNotified
        val fullAlreadySent = sameDay && state.fullNotified

        when {
            count >= limit && !fullAlreadySent -> {
                show(
                    id = ID_LIMIT,
                    channel = CHANNEL_LIMITS,
                    title = "Daily limit reached",
                    text = "$count reels today - that is all $limit you gave yourself.",
                )
                settingsRepository.markAlertSent(today, half = true, full = true)
            }

            count >= limit / 2 && !halfAlreadySent -> {
                show(
                    id = ID_LIMIT,
                    channel = CHANNEL_LIMITS,
                    title = "Halfway to your limit",
                    text = "$count of $limit reels today.",
                )
                settingsRepository.markAlertSent(today, half = true, full = false)
            }
        }
    }

    /**
     * The evening recap, sent by DailySummaryWorker. Says nothing at all on a
     * day with no scrolling in either direction - silence is the reward.
     */
    suspend fun sendDailySummary() {
        val settings = settingsRepository.current()
        if (!settings.notificationsEnabled || !canPost()) return

        val today = TimeRanges.today()
        val state = settingsRepository.notificationStateNow()
        if (state.summarySentOnDay == today.toEpochDay()) return

        val todayCount = reelRepository.countForDay(today)
        val yesterdayCount = reelRepository.countForDay(today.minusDays(1))
        if (todayCount == 0 && yesterdayCount == 0) return

        show(
            id = ID_SUMMARY,
            channel = CHANNEL_SUMMARY,
            title = "$todayCount reels today",
            text = comparisonText(todayCount, yesterdayCount),
        )
        settingsRepository.markSummarySent(today.toEpochDay())
    }

    private fun comparisonText(today: Int, yesterday: Int): String {
        if (yesterday == 0) return "Your first day of counting. Tomorrow you will have something to beat."
        val difference = today - yesterday
        return when {
            difference == 0 -> "Exactly the same as yesterday."
            difference < 0 -> "${abs(difference)} fewer than yesterday. Nice."
            else -> "$difference more than yesterday."
        }
    }

    private fun show(id: Int, channel: String, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // The user revoked notification permission between the check and
            // here. Nothing to do - staying quiet is the correct behaviour.
        }
    }

    private fun canPost(): Boolean = manager.areNotificationsEnabled()

    private companion object {
        const val CHANNEL_LIMITS = "limit_alerts"
        const val CHANNEL_SUMMARY = "daily_summary"
        const val ID_LIMIT = 1001
        const val ID_SUMMARY = 1002
    }
}
