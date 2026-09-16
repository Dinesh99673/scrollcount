package com.probuilder.scrollcount.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * All date maths for the app lives here.
 *
 * Reels are stored as plain epoch-millisecond timestamps. Turning those into
 * "today", "this hour" or "last Tuesday" has to happen in the phone's own time
 * zone, otherwise someone scrolling at 1 AM would see their reels land on the
 * wrong day. Every function below goes through ZoneId.systemDefault() so the
 * whole app agrees on where a day starts and ends.
 */
object TimeRanges {

    private fun zone(): ZoneId = ZoneId.systemDefault()

    fun now(): Long = System.currentTimeMillis()

    fun today(): LocalDate = LocalDate.now(zone())

    /** Midnight at the start of [date], as epoch millis. */
    fun startOf(date: LocalDate): Long =
        date.atStartOfDay(zone()).toInstant().toEpochMilli()

    /** Midnight at the start of the following day (the exclusive end of [date]). */
    fun endOf(date: LocalDate): Long = startOf(date.plusDays(1))

    fun startOfToday(): Long = startOf(today())

    fun endOfToday(): Long = endOf(today())

    /** Which calendar day a stored timestamp belongs to. */
    fun dateOf(timestamp: Long): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(zone()).toLocalDate()

    /** Hour of the day (0-23) a stored timestamp belongs to. */
    fun hourOf(timestamp: Long): Int =
        Instant.ofEpochMilli(timestamp).atZone(zone()).hour

    /** The last [days] calendar days, oldest first, ending with today. */
    fun lastDays(days: Int): List<LocalDate> {
        val today = today()
        return (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }
}
