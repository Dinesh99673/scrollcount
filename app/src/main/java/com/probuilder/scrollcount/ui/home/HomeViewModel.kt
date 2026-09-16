package com.probuilder.scrollcount.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.TrackedApps
import com.probuilder.scrollcount.data.settings.SettingsRepository
import com.probuilder.scrollcount.util.Formatting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One row in the "where did they come from" list. */
data class AppBreakdown(
    val packageName: String,
    val displayName: String,
    val itemName: String,
    val count: Int,
    val tracked: Boolean,
)

data class HomeUiState(
    val todayTotal: Int = 0,
    val breakdown: List<AppBreakdown> = emptyList(),
    val dailyLimit: Int = 100,
    val estimatedSeconds: Int = 0,
    val percentOfLimit: Int = 0,
    val loaded: Boolean = false,
)

/**
 * Feeds the home screen.
 *
 * Everything here is derived from two live database queries and the settings
 * flow, so the number on screen changes the instant the service records a reel
 * in another app.
 */
class HomeViewModel(
    reelRepository: ReelRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        reelRepository.observeTodayTotal(),
        reelRepository.observeTodayByApp(),
        settingsRepository.settings,
    ) { total, countsByApp, settings ->
        HomeUiState(
            todayTotal = total,
            breakdown = TrackedApps.all.map { app ->
                AppBreakdown(
                    packageName = app.packageName,
                    displayName = app.displayName,
                    itemName = app.itemName,
                    count = countsByApp[app.packageName] ?: 0,
                    tracked = settings.isTracked(app.packageName),
                )
            },
            dailyLimit = settings.dailyLimit,
            estimatedSeconds = Formatting.estimatedSeconds(total, settings.secondsPerReel),
            percentOfLimit = Formatting.percentOfLimit(total, settings.dailyLimit),
            loaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = HomeUiState(),
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
