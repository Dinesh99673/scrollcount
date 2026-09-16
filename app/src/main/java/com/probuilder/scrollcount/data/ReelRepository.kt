package com.probuilder.scrollcount.data

import com.probuilder.scrollcount.data.db.ReelEvent
import com.probuilder.scrollcount.data.db.ReelEventDao
import com.probuilder.scrollcount.util.TimeRanges
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** A single day's total, used by the 7-day chart. */
data class DayTotal(val date: LocalDate, val count: Int)

/**
 * The one place the rest of the app asks about reel counts.
 *
 * Screens never touch the DAO directly. That keeps the date logic (what "today"
 * means, how weeks are grouped) in a single file, and means the service can
 * record a reel with one short call.
 */
class ReelRepository(private val dao: ReelEventDao) {

    /** Called by the accessibility service each time a new reel appears. */
    suspend fun recordReel(appPackage: String, timestamp: Long = TimeRanges.now()) {
        dao.insert(ReelEvent(appPackage = appPackage, timestamp = timestamp))
    }

    /**
     * Emits the start of the current day, and emits again after midnight.
     *
     * Without this, an app left open overnight would keep showing yesterday's
     * "today". The one-minute tick is cheap and distinctUntilChanged means the
     * database is only re-queried when the day actually rolls over.
     */
    private fun todayStart(): Flow<Long> = flow {
        while (true) {
            emit(TimeRanges.startOfToday())
            delay(TICK_MS)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayTotal(): Flow<Int> = todayStart().flatMapLatest { start ->
        dao.observeCountBetween(start, TimeRanges.endOfToday())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayByApp(): Flow<Map<String, Int>> = todayStart().flatMapLatest { start ->
        dao.observeCountsByApp(start, TimeRanges.endOfToday())
            .map { rows -> rows.associate { it.appPackage to it.count } }
    }

    /**
     * Reels per hour for today, always 24 entries so the chart keeps a stable
     * shape even early in the morning.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayByHour(): Flow<List<Int>> = todayStart().flatMapLatest { start ->
        dao.observeBetween(start, TimeRanges.endOfToday()).map { events ->
            val buckets = IntArray(24)
            events.forEach { buckets[TimeRanges.hourOf(it.timestamp)]++ }
            buckets.toList()
        }
    }

    /**
     * Daily totals for the last [days] days, oldest first. Days with no reels
     * are included as zero so the bar chart has no gaps.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLastDays(days: Int = 7): Flow<List<DayTotal>> = todayStart().flatMapLatest {
        val dates = TimeRanges.lastDays(days)
        val start = TimeRanges.startOf(dates.first())
        dao.observeBetween(start, TimeRanges.endOfToday()).map { events ->
            val byDate = events.groupingBy { TimeRanges.dateOf(it.timestamp) }.eachCount()
            dates.map { date -> DayTotal(date, byDate[date] ?: 0) }
        }
    }

    /** Live count of every reel ever recorded, shown on the settings screen. */
    fun observeAllTimeTotal(): Flow<Int> = dao.observeCountBetween(0L, Long.MAX_VALUE)

    /** One-shot count for today, used by notifications. */
    suspend fun todayCount(): Int =
        dao.countBetween(TimeRanges.startOfToday(), TimeRanges.endOfToday())

    /** One-shot count for a given calendar day. */
    suspend fun countForDay(date: LocalDate): Int =
        dao.countBetween(TimeRanges.startOf(date), TimeRanges.endOf(date))

    suspend fun totalCount(): Int = dao.totalCount()

    /** Date of the first reel ever recorded, or null on a fresh install. */
    suspend fun firstRecordedDate(): LocalDate? =
        dao.firstTimestamp()?.let { TimeRanges.dateOf(it) }

    suspend fun clearAll() = dao.deleteAll()

    private companion object {
        const val TICK_MS = 60_000L
    }
}
