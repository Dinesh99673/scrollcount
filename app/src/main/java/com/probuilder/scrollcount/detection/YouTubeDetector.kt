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

    // Confirmed against YouTube 21.36.45 by dumping the view tree on a real
    // device. reel_watch, shorts_video and shorts_shelf were all guesses that
    // do not exist in this version (0 hits), so they have been removed.
    override val screenMarkers: List<String> = listOf(
        "reel_recycler",             // the vertical pager holding each short (10 hits)
        "reel_player_page",          // one short's page container (11 hits)
    )

    override val pagerMarkers: List<String> = listOf("reel_recycler")

    /**
     * YouTube reports no item index at all - every scroll event from
     * reel_recycler came back with fromIndex = -1 - so an event from the pager
     * has to be taken at face value. This is weaker than Instagram's index and
     * is the reason Shorts counting is still being tuned.
     */
    override val countsIndexlessPagerEvents: Boolean = true

    /** With no index to work from, the short showing on entry would be missed. */
    override val countsOnScreenEntry: Boolean = true
}
