package com.probuilder.scrollcount.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per reel or short that appeared on screen.
 *
 * Storing every reel separately (instead of a running total) is what makes the
 * stats screen possible later: daily bars, hourly patterns and "busiest day"
 * are all just different ways of grouping these timestamps.
 *
 * Note what is NOT here: no caption, no username, no video id, no title.
 * The app only ever knows that *something* was scrolled past, and when.
 */
@Entity(tableName = "reel_events", indices = [Index("timestamp")])
data class ReelEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Package name of the app it happened in, e.g. com.instagram.android. */
    @ColumnInfo(name = "appPackage") val appPackage: String,
    /** When it happened, epoch millis. */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
)

/** Result row for "how many reels per app" queries. */
data class AppCount(
    @ColumnInfo(name = "appPackage") val appPackage: String,
    @ColumnInfo(name = "count") val count: Int,
)
