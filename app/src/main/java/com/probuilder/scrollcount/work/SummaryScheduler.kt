package com.probuilder.scrollcount.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Books the daily summary notification for roughly 9 PM every evening.
 *
 * WorkManager survives reboots and force-stops on its own, so this only needs
 * calling once at app start; asking again simply refreshes the schedule.
 */
object SummaryScheduler {

    private const val WORK_NAME = "scrollcount_daily_summary"
    private const val SUMMARY_HOUR = 21

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNextSummary(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /** Milliseconds from now until the next 9 PM in the phone's own time zone. */
    private fun millisUntilNextSummary(): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val todayAtNine = LocalDateTime.of(LocalDate.now(zone), LocalTime.of(SUMMARY_HOUR, 0))
        val target = if (now.isBefore(todayAtNine)) todayAtNine else todayAtNine.plusDays(1)
        val nowMillis = now.atZone(zone).toInstant().toEpochMilli()
        val targetMillis = target.atZone(zone).toInstant().toEpochMilli()
        return (targetMillis - nowMillis).coerceAtLeast(0L)
    }
}
