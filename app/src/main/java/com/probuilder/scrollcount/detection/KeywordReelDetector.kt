package com.probuilder.scrollcount.detection

import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Shared machinery for every detector.
 *
 * Both supported apps are recognised the same way: the full-screen reel player
 * puts a handful of very distinctive view IDs on screen, so if any of those IDs
 * exist in the current window, the user is watching reels. Each app subclass
 * only has to supply its own list of ID fragments.
 *
 * How to update these lists when an app update breaks counting:
 *   1. Turn on "Developer logging" in Settings (debug builds only).
 *   2. Open the reel screen and watch Logcat with the tag "ScrollDebug".
 *   3. Find a view ID that appears there and nowhere else in the app.
 *   4. Add that fragment to screenMarkers below.
 */
abstract class KeywordReelDetector(
    override val packageName: String,
    override val appName: String,
) : ReelDetector {

    /**
     * Fragments of view IDs that only exist on the full-screen reel player.
     * Matching is a case-insensitive "contains", so short fragments are fine.
     */
    protected abstract val screenMarkers: List<String>

    /**
     * Fragments that mean "this is definitely NOT the reel player" even if a
     * marker also matched, e.g. a comments sheet drawn over the player.
     */
    protected open val blockingMarkers: List<String> = emptyList()

    /**
     * Fragments identifying the vertical pager that actually holds the reels.
     *
     * This is the fix for the worst bug this app has had. While the reel player
     * is open, these apps fire scroll events from several other lists at once -
     * on Instagram, 181 of 218 scroll events in one test session came from
     * android:id/list, not the reel pager. Counting all of them turned 10 reels
     * into 34. Only events from the views named here are ever counted.
     */
    protected abstract val pagerMarkers: List<String>

    /**
     * What to do when the pager reports a scroll but no item index.
     *
     * Instagram gives a clean index per reel, so an index-less event there is
     * noise and is ignored. YouTube gives no index at all, so for it an event
     * from the pager is the only signal available and has to be trusted, with
     * the service debounce as the only protection against double counting.
     */
    protected open val countsIndexlessPagerEvents: Boolean = false

    /** Position of the reel that was on screen the last time we looked. */
    private var lastPosition: Int = POSITION_UNKNOWN

    override fun isOnReelScreen(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Boolean {
        // Cheap path first: the scrolling view's own ancestors usually carry the
        // marker, and walking a dozen parents costs far less than a full scan.
        if (matchesAncestors(event)) return true
        val tree = root ?: return false
        val scan = scanWindow(tree)
        return scan.foundMarker && !scan.foundBlocker
    }

    override fun isNewReel(event: AccessibilityEvent): Boolean {
        val isScroll = event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
        // A ViewPager announces a page change as "selected", which some apps
        // emit even when they emit no scroll event at all.
        val isSelected = event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED
        if (!isScroll && !isSelected) return false

        // The single most important check: did this come from the reel pager,
        // or from one of the other lists sharing the screen with it?
        if (!isFromPager(event)) return false

        // Reels scroll vertically. Anything moving sideways is a carousel,
        // a tab strip or a swipe between profiles, and must not be counted.
        if (isScroll && isHorizontalScroll(event)) return false

        val position = positionOf(event)
        if (position != POSITION_UNKNOWN) {
            val changed = position != lastPosition
            lastPosition = position
            return changed
        }
        return countsIndexlessPagerEvents
    }

    override fun onLeftReelScreen() {
        lastPosition = POSITION_UNKNOWN
    }

    // --- helpers ------------------------------------------------------------

    private data class Scan(val foundMarker: Boolean, val foundBlocker: Boolean)

    /**
     * Breadth-first walk of the window looking for marker IDs. Bounded by node
     * count and depth so a huge screen can never freeze the UI thread.
     */
    private fun scanWindow(root: AccessibilityNodeInfo): Scan {
        var foundMarker = false
        var foundBlocker = false
        var visited = 0
        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.addLast(root to 0)
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val (node, depth) = queue.removeFirst()
            visited++
            val id = node.viewIdResourceName
            if (id != null) {
                if (!foundMarker && matches(id, screenMarkers)) foundMarker = true
                if (!foundBlocker && matches(id, blockingMarkers)) foundBlocker = true
                if (foundMarker && foundBlocker) return Scan(true, true)
            }
            if (depth >= MAX_DEPTH) continue
            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                queue.addLast(child to depth + 1)
            }
        }
        return Scan(foundMarker, foundBlocker)
    }

    /** Looks for a marker on the scrolling view itself and its parents. */
    private fun matchesAncestors(event: AccessibilityEvent): Boolean {
        var node: AccessibilityNodeInfo? = try {
            event.source
        } catch (_: Exception) {
            null
        }
        var steps = 0
        while (node != null && steps < MAX_ANCESTORS) {
            val id = node.viewIdResourceName
            if (id != null) {
                if (matches(id, blockingMarkers)) return false
                if (matches(id, screenMarkers)) return true
            }
            node = node.parent
            steps++
        }
        return false
    }

    private fun matches(viewId: String, fragments: List<String>): Boolean =
        fragments.any { viewId.contains(it, ignoreCase = true) }

    /** True when the event came from the reel pager itself, not a neighbouring list. */
    private fun isFromPager(event: AccessibilityEvent): Boolean {
        if (pagerMarkers.isEmpty()) return true
        val sourceId = try {
            event.source?.viewIdResourceName
        } catch (_: Exception) {
            null
        } ?: return false
        return matches(sourceId, pagerMarkers)
    }

    /**
     * Which item the scrolling container moved to. RecyclerView and ViewPager2
     * both report this as fromIndex; anything else returns "unknown".
     */
    private fun positionOf(event: AccessibilityEvent): Int {
        val from = event.fromIndex
        return if (from >= 0) from else POSITION_UNKNOWN
    }

    private fun isHorizontalScroll(event: AccessibilityEvent): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val deltaX = event.scrollDeltaX
        val deltaY = event.scrollDeltaY
        return deltaY == 0 && deltaX != 0
    }

    protected companion object {
        const val POSITION_UNKNOWN = -1
        private const val MAX_NODES = 400
        private const val MAX_DEPTH = 25
        private const val MAX_ANCESTORS = 12
    }
}
