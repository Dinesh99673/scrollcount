package com.probuilder.scrollcount.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Every database read and write the app performs.
 *
 * Day boundaries are passed in as epoch-millisecond ranges rather than computed
 * in SQL, because SQLite's date functions do not know about the phone's time
 * zone. TimeRanges works them out in Kotlin and hands them over.
 *
 * The functions returning Flow re-run themselves whenever the table changes, so
 * the home screen updates the moment the service records a reel.
 */
@Dao
interface ReelEventDao {

    @Insert
    suspend fun insert(event: ReelEvent)

    /** Live count for a time window, e.g. today. */
    @Query("SELECT COUNT(*) FROM reel_events WHERE timestamp >= :start AND timestamp < :end")
    fun observeCountBetween(start: Long, end: Long): Flow<Int>

    /** One-shot version of the above, for notifications and background work. */
    @Query("SELECT COUNT(*) FROM reel_events WHERE timestamp >= :start AND timestamp < :end")
    suspend fun countBetween(start: Long, end: Long): Int

    /** Live per-app breakdown for a time window. */
    @Query(
        "SELECT appPackage AS appPackage, COUNT(*) AS count FROM reel_events " +
            "WHERE timestamp >= :start AND timestamp < :end GROUP BY appPackage"
    )
    fun observeCountsByApp(start: Long, end: Long): Flow<List<AppCount>>

    /**
     * Raw rows for a window, used by the charts. A week of heavy scrolling is
     * only a few thousand rows, so grouping them into days or hours in Kotlin
     * is both fast enough and easier to get right across time zones.
     */
    @Query("SELECT * FROM reel_events WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun observeBetween(start: Long, end: Long): Flow<List<ReelEvent>>

    /** Timestamp of the very first reel ever recorded, or null if there are none. */
    @Query("SELECT MIN(timestamp) FROM reel_events")
    suspend fun firstTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM reel_events")
    suspend fun totalCount(): Int

    @Query("DELETE FROM reel_events")
    suspend fun deleteAll()
}
