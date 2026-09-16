package com.probuilder.scrollcount.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Day boundaries are the easiest thing in this app to get subtly wrong, so the
 * rules are pinned down here: a day runs from local midnight to local midnight,
 * and a timestamp always lands on the day the user would say it did.
 */
class TimeRangesTest {

    @Test
    fun `a day range starts at local midnight and ends at the next one`() {
        val date = LocalDate.of(2026, 3, 14)
        val start = TimeRanges.startOf(date)
        val end = TimeRanges.endOf(date)

        assertEquals(date, TimeRanges.dateOf(start))
        assertEquals(date.plusDays(1), TimeRanges.dateOf(end))
        assertTrue(end > start)
    }

    @Test
    fun `the last millisecond of a day still belongs to that day`() {
        val date = LocalDate.of(2026, 3, 14)
        val lastMoment = TimeRanges.endOf(date) - 1
        assertEquals(date, TimeRanges.dateOf(lastMoment))
    }

    @Test
    fun `hourOf reads the hour in the phone time zone`() {
        val date = LocalDate.of(2026, 3, 14)
        val nineInTheEvening = date.atTime(21, 30)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals(21, TimeRanges.hourOf(nineInTheEvening))
    }

    @Test
    fun `lastDays returns the requested number of days ending with today`() {
        val days = TimeRanges.lastDays(7)

        assertEquals(7, days.size)
        assertEquals(TimeRanges.today(), days.last())
        assertEquals(TimeRanges.today().minusDays(6), days.first())
    }

    @Test
    fun `lastDays comes back in chronological order`() {
        val days = TimeRanges.lastDays(7)
        days.zipWithNext { earlier, later ->
            assertTrue(earlier.isBefore(later))
        }
    }
}
