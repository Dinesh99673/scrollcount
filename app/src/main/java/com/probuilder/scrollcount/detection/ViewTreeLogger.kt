package com.probuilder.scrollcount.detection

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.probuilder.scrollcount.BuildConfig

/**
 * The tool used to work out which view IDs identify a reel screen.
 *
 * Turn on "Developer logging" in Settings (debug builds only), then watch
 * Logcat filtered by the tag "ScrollDebug" while you:
 *   1. scroll the normal feed,
 *   2. open Reels or Shorts and swipe a few times,
 *   3. open Stories or the home feed again.
 *
 * Compare the three dumps and look for a view ID that appears only in step 2.
 * That ID goes into the matching detector's screenMarkers list.
 *
 * This never runs in a release build: every entry point checks BuildConfig.DEBUG
 * first, so no user's screen contents are ever written to a log.
 */
object ViewTreeLogger {

    const val TAG = "ScrollDebug"

    private var lastTreeDumpAt = 0L
    private const val TREE_DUMP_INTERVAL_MS = 1500L
    private const val MAX_DEPTH = 20
    private const val MAX_NODES = 300

    /** One short line per accessibility event. */
    fun logEvent(event: AccessibilityEvent) {
        if (!BuildConfig.DEBUG) return
        val type = AccessibilityEvent.eventTypeToString(event.eventType)
        Log.d(
            TAG,
            "event pkg=${event.packageName} type=$type class=${event.className} " +
                "fromIndex=${event.fromIndex} toIndex=${event.toIndex} itemCount=${event.itemCount}",
        )
    }

    /**
     * Prints the whole window as an indented tree of view IDs. Throttled,
     * because a single swipe can fire dozens of events and each dump is long.
     */
    fun logTree(root: AccessibilityNodeInfo?, elapsedRealtime: Long) {
        if (!BuildConfig.DEBUG || root == null) return
        if (elapsedRealtime - lastTreeDumpAt < TREE_DUMP_INTERVAL_MS) return
        lastTreeDumpAt = elapsedRealtime

        Log.d(TAG, "--- view tree for ${root.packageName} ---")
        var printed = 0
        val stack = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        stack.addLast(root to 0)
        while (stack.isNotEmpty() && printed < MAX_NODES) {
            val (node, depth) = stack.removeLast()
            printed++
            val indent = "  ".repeat(depth)
            val id = node.viewIdResourceName ?: "(no id)"
            val scrollable = if (node.isScrollable) " SCROLLABLE" else ""
            Log.d(TAG, "$indent$id  [${node.className}]$scrollable")
            if (depth >= MAX_DEPTH) continue
            // Push children in reverse so they print top-to-bottom.
            for (index in node.childCount - 1 downTo 0) {
                val child = node.getChild(index) ?: continue
                stack.addLast(child to depth + 1)
            }
        }
        Log.d(TAG, "--- end of tree ($printed nodes) ---")
    }
}
