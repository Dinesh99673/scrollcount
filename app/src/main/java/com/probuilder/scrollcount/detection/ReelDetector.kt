package com.probuilder.scrollcount.detection

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * One detector per supported app.
 *
 * Instagram and YouTube change their layouts often, and when they do only the
 * view IDs move around. Keeping each app behind this small interface means a
 * broken app update is a one-file fix, not a rewrite of the service.
 */
interface ReelDetector {

    /** The app this detector understands, e.g. com.instagram.android. */
    val packageName: String

    /** Human-readable name used in logs and on screen. */
    val appName: String

    /**
     * Whether opening the reel player should itself count as one reel.
     *
     * False for apps whose pager reports an item index as soon as it appears -
     * that first index already counts the reel on screen, so counting on entry
     * too would double it. True for apps that give us nothing to go on, where
     * counting on entry is better than missing the reel entirely.
     */
    val countsOnScreenEntry: Boolean

    /**
     * True when the user is currently looking at the full-screen Reels/Shorts
     * player, as opposed to the normal feed, stories, comments or search.
     *
     * [root] is the current window's view tree. It may be null when the system
     * will not hand it over, in which case the detector falls back to whatever
     * the event itself reveals.
     */
    fun isOnReelScreen(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Boolean

    /**
     * True when this event means a *different* reel is now on screen.
     *
     * The service still applies its own debounce on top of this, so returning
     * true a little too eagerly is safe.
     */
    fun isNewReel(event: AccessibilityEvent): Boolean

    /** Called when the user leaves the reel screen, so the detector can reset. */
    fun onLeftReelScreen() = Unit
}
