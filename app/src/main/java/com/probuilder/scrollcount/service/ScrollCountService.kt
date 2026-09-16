package com.probuilder.scrollcount.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.probuilder.scrollcount.BuildConfig
import com.probuilder.scrollcount.ScrollCountApp
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.settings.AppSettings
import com.probuilder.scrollcount.data.settings.SettingsRepository
import com.probuilder.scrollcount.detection.DetectorRegistry
import com.probuilder.scrollcount.detection.ReelDetector
import com.probuilder.scrollcount.detection.ViewTreeLogger
import com.probuilder.scrollcount.notifications.ReelNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The part of ScrollCount that watches for reels.
 *
 * What it actually does, in order, for every event Android hands it:
 *   1. work out which app the event came from and pick that app's detector,
 *   2. ask the detector whether the reel player is on screen at all,
 *   3. if it is, ask whether a *different* reel just appeared,
 *   4. if so, add one row to the database.
 *
 * What it never does: read text, captions, usernames, messages or video
 * content. The only thing that leaves this file is "app X, at time T".
 *
 * Everything in onAccessibilityEvent runs on the main thread, so the expensive
 * parts are either bounded (the detector's view scan) or pushed onto a
 * background coroutine (the database write).
 */
class ScrollCountService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val registry = DetectorRegistry()

    private lateinit var reelRepository: ReelRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var notifier: ReelNotifier
    private var initialised = false

    /**
     * The latest settings, kept in a field so the event handler can read them
     * instantly instead of suspending on the main thread.
     */
    @Volatile
    private var settings: AppSettings = AppSettings()

    private var activeDetector: ReelDetector? = null
    private var onReelScreen = false
    private var cachedOnReelScreen = false
    private var lastScreenCheckAt = 0L
    private var lastCountAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val app = application as? ScrollCountApp
        if (app == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Application is not ScrollCountApp; service cannot record reels.")
            }
            return
        }
        reelRepository = app.container.reelRepository
        settingsRepository = app.container.settingsRepository
        notifier = app.container.notifier
        initialised = true

        serviceScope.launch {
            settingsRepository.settings.collect { settings = it }
        }

        ScrollCountServiceState.setRunning(true)
        if (BuildConfig.DEBUG) Log.i(TAG, "ScrollCount service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!initialised || event == null) return
        val packageName = event.packageName?.toString() ?: return

        val detector = registry.forPackage(packageName)
        if (detector == null) {
            leaveReelScreen()
            return
        }

        if (BuildConfig.DEBUG && settings.debugLogging) {
            ViewTreeLogger.logEvent(event)
            ViewTreeLogger.logTree(safeRoot(), SystemClock.elapsedRealtime())
        }

        // The user turned this app off in Settings, so ignore it entirely.
        if (!settings.isTracked(packageName)) {
            leaveReelScreen()
            return
        }

        // Switched from one tracked app straight to the other.
        val previous = activeDetector
        if (previous != null && previous !== detector) leaveReelScreen()

        if (!isOnReelScreen(detector, event)) {
            leaveReelScreen()
            return
        }

        if (!onReelScreen) {
            onReelScreen = true
            activeDetector = detector
            // Only apps that give us no item index count the reel on screen at
            // the moment the player opens. Where an index is available, that
            // index counts it instead - see ReelDetector.countsOnScreenEntry.
            if (detector.countsOnScreenEntry) {
                countReel(detector, "opened")
                return
            }
        }

        if (detector.isNewReel(event)) countReel(detector, "swiped")
    }

    /**
     * Whether the reel player is on screen, re-checked at most a few times a
     * second. Reading the whole window on every scroll event would be far too
     * expensive, and the answer cannot change faster than the user can navigate.
     */
    private fun isOnReelScreen(detector: ReelDetector, event: AccessibilityEvent): Boolean {
        val now = SystemClock.elapsedRealtime()
        val windowChanged = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        if (!windowChanged && now - lastScreenCheckAt < SCREEN_RECHECK_MS) return cachedOnReelScreen

        lastScreenCheckAt = now
        cachedOnReelScreen = detector.isOnReelScreen(event, safeRoot())
        return cachedOnReelScreen
    }

    /**
     * Records one reel, ignoring anything that arrives too soon after the last
     * one. A single swipe fires a burst of events, and this is what turns that
     * burst into exactly one count.
     */
    private fun countReel(detector: ReelDetector, reason: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastCountAt < DEBOUNCE_MS) return
        lastCountAt = now

        ScrollCountServiceState.incrementSessionCount()
        if (BuildConfig.DEBUG) {
            Log.d(
                ViewTreeLogger.TAG,
                "Reel counted: ${detector.appName} ($reason), " +
                    "session total = ${ScrollCountServiceState.sessionCount.value}",
            )
        }

        val packageName = detector.packageName
        serviceScope.launch {
            reelRepository.recordReel(packageName)
            notifier.onReelCounted()
        }
    }

    private fun leaveReelScreen() {
        if (onReelScreen) activeDetector?.onLeftReelScreen()
        onReelScreen = false
        cachedOnReelScreen = false
        activeDetector = null
    }

    /** rootInActiveWindow throws or returns null while windows are changing. */
    private fun safeRoot() = try {
        rootInActiveWindow
    } catch (_: Exception) {
        null
    }

    override fun onInterrupt() {
        leaveReelScreen()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        ScrollCountServiceState.setRunning(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveReelScreen()
        registry.resetAll()
        serviceScope.cancel()
        ScrollCountServiceState.setRunning(false)
        if (BuildConfig.DEBUG) Log.i(TAG, "ScrollCount service destroyed.")
    }

    private companion object {
        /**
         * Only ever used inside BuildConfig.DEBUG checks. A release build of
         * ScrollCount writes nothing to Logcat at all, not even its own
         * lifecycle, so there is no trail of when you were watching reels.
         */
        const val TAG = "ScrollCountService"

        /**
         * A single swipe can fire many events, so counts are rate limited.
         *
         * This used to be 700ms and was doing most of the work. Now that
         * Instagram is deduplicated by pager item index, the debounce is only a
         * backstop, so it can be shorter and stop penalising fast swiping.
         * YouTube, which has no index, still leans on it.
         */
        const val DEBOUNCE_MS = 400L

        /** How often the "is this the reel screen" question is asked again. */
        const val SCREEN_RECHECK_MS = 400L
    }
}
