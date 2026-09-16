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

    // Confirmed against Instagram 446.0.0.49.77 by dumping the view tree on a
    // real device. Hit counts over a 6-minute session are in the comments.
    override val screenMarkers: List<String> = listOf(
        "clips_viewer_view_pager",   // the full-screen reels pager itself (155 hits)
        "clips_video_container",     // the video surface inside it (152 hits)
    )

    // The reels pager. Verified to report a clean item index per reel: a test
    // session of 10 swipes produced exactly the sequence 1..10 from this view,
    // while 181 other scroll events came from android:id/list and are ignored.
    override val pagerMarkers: List<String> = listOf("clips_viewer_view_pager")

    // Opening the player already produces an index from the pager, so counting
    // the entry as well would count the first reel twice.
    override val countsOnScreenEntry: Boolean = false

    // Deliberately NOT markers, even though they contain "clips":
    //   clips_tab - the Reels button in the bottom navigation bar. It is on
    //     screen on the home feed too, so matching it made ScrollCount think
    //     the normal feed was the reel player and count feed scrolling.
    //   clips_swipe_refresh - does not exist in this version (0 hits).

    override val blockingMarkers: List<String> = listOf(
        "reel_viewer",               // Stories, not Reels
        "story_viewer",
    )
}
