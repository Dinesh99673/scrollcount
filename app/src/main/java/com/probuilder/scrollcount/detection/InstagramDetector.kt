package com.probuilder.scrollcount.detection

import com.probuilder.scrollcount.data.TrackedApps

/**
 * Recognises the Instagram Reels player.
 *
 * Important quirk: inside Instagram's code, Reels are called "clips" and the
 * word "reel" is used for *Stories*. So matching on "clips" is what keeps
 * Stories out of your count, and matching on "reel" would break it.
 *
 * These IDs come from Instagram's own layout files and do change between app
 * versions. If counting ever stops working, follow the steps in
 * KeywordReelDetector's comment to find the new ones.
 */
class InstagramDetector : KeywordReelDetector(
    packageName = TrackedApps.INSTAGRAM,
    appName = "Instagram",
) {

    override val screenMarkers: List<String> = listOf(
        "clips_viewer",              // the full-screen reels pager
        "clips_video_container",     // the video surface inside it
        "clips_swipe_refresh",       // pull-to-refresh wrapper around the pager
        "clips_tab",                 // the reels tab host
    )

    override val blockingMarkers: List<String> = listOf(
        "reel_viewer",               // Stories, not Reels
        "story_viewer",
    )
}
