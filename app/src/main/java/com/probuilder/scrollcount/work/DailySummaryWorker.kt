package com.probuilder.scrollcount.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.probuilder.scrollcount.ScrollCountApp

/**
 * Runs once each evening and asks ReelNotifier to send the daily recap.
 *
 * All the "should this even be sent" logic lives in the notifier, so this class
 * stays a one-liner and the rules only exist in one place.
 */
class DailySummaryWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? ScrollCountApp ?: return Result.success()
        return try {
            app.container.notifier.sendDailySummary()
            Result.success()
        } catch (_: Exception) {
            // A missed summary is not worth retrying aggressively; the next
            // day's run will cover it.
            Result.success()
        }
    }
}
