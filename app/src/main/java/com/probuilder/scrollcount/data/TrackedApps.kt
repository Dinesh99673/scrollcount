package com.probuilder.scrollcount.data

/**
 * The apps ScrollCount knows how to count, in one place.
 *
 * These package names appear in three other places - the accessibility config,
 * the detectors and the settings screen - so they are defined once here to stop
 * them drifting apart.
 */
object TrackedApps {

    const val INSTAGRAM = "com.instagram.android"
    const val YOUTUBE = "com.google.android.youtube"

    data class TrackedApp(
        val packageName: String,
        val displayName: String,
        /** What a single item on that app's feed is called, e.g. "Reels". */
        val itemName: String,
    )

    val all: List<TrackedApp> = listOf(
        TrackedApp(INSTAGRAM, "Instagram", "Reels"),
        TrackedApp(YOUTUBE, "YouTube", "Shorts"),
    )

    fun displayName(packageName: String): String =
        all.firstOrNull { it.packageName == packageName }?.displayName ?: packageName

    fun itemName(packageName: String): String =
        all.firstOrNull { it.packageName == packageName }?.itemName ?: "Videos"
}
