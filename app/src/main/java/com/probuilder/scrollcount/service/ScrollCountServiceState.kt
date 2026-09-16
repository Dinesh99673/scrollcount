package com.probuilder.scrollcount.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lets the UI know whether the accessibility service is actually running right
 * now, so the home screen can show its "counting is paused" banner.
 *
 * This is deliberately a plain object rather than something injected: the
 * service is created by the Android system, not by the app, so there is no
 * constructor to pass anything into.
 */
object ScrollCountServiceState {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    /** Number of reels counted since the service last started. Debug aid. */
    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    internal fun setRunning(running: Boolean) {
        _isRunning.value = running
        if (!running) _sessionCount.value = 0
    }

    internal fun incrementSessionCount() {
        _sessionCount.value += 1
    }
}
