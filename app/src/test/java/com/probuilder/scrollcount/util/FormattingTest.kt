package com.probuilder.scrollcount.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the number-to-text helpers. These are the bits users read most
 * often, so they are worth pinning down.
 */
class FormattingTest {

    @Test
    fun `duration under a minute is shown in seconds`() {
        assertEquals("40s", Formatting.duration(40))
    }

    @Test
    fun `duration under an hour is shown in whole minutes`() {
        assertEquals("12m", Formatting.duration(12 * 60 + 30))
    }

    @Test
    fun `duration of exactly one hour drops the minutes`() {
        assertEquals("1h", Formatting.duration(3600))
    }

    @Test
    fun `duration over an hour shows hours and minutes`() {
        assertEquals("1h 24m", Formatting.duration(3600 + 24 * 60))
    }

    @Test
    fun `estimated seconds multiplies count by seconds per reel`() {
        assertEquals(400, Formatting.estimatedSeconds(reelCount = 20, secondsPerReel = 20))
    }

    @Test
    fun `percent of limit rounds to the nearest whole percent`() {
        assertEquals(50, Formatting.percentOfLimit(count = 50, limit = 100))
        assertEquals(33, Formatting.percentOfLimit(count = 1, limit = 3))
    }

    @Test
    fun `percent of limit is clamped so the layout never breaks`() {
        assertEquals(999, Formatting.percentOfLimit(count = 10_000, limit = 10))
    }

    @Test
    fun `percent of limit copes with a zero limit`() {
        assertEquals(0, Formatting.percentOfLimit(count = 5, limit = 0))
    }

    @Test
    fun `hour labels read the way people speak`() {
        assertEquals("12 AM", Formatting.hourLabel(0))
        assertEquals("9 AM", Formatting.hourLabel(9))
        assertEquals("12 PM", Formatting.hourLabel(12))
        assertEquals("11 PM", Formatting.hourLabel(23))
    }
}
