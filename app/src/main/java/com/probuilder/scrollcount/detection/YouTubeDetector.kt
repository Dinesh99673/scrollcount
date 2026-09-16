package com.probuilder.scrollcount.detection

import com.probuilder.scrollcount.data.TrackedApps

/**
 * Recognises the YouTube Shorts player.
 *
 * YouTube's internal name for Shorts is "reel", which is why these IDs look
 * the way they do. The markers are deliberately specific to the full-screen
 * player, so scrolling past the Shorts shelf on the home feed is not counted.
 */
class YouTubeDetector : KeywordReelDetector(
    packageName = TrackedApps.YOUTUBE,
    appName = "YouTube",
) {

    override val screenMarkers: List<String> = listOf(
        "reel_recycler",             // the vertical pager holding each short
        "reel_player_page",          // one short's page
        "reel_watch",                // the shorts watch fragment root
        "shorts_video",              // newer builds
    )

    override val blockingMarkers: List<String> = listOf(
        "shorts_shelf",              // the Shorts row on the home feed
    )
}
