package com.probuilder.scrollcount.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.probuilder.scrollcount.data.ReelRepository
import com.probuilder.scrollcount.data.settings.AppSettings
import com.probuilder.scrollcount.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val totalEverCounted: Int = 0,
)

/**
 * Backs the settings screen. Every change is written straight to DataStore, so
 * the accessibility service picks it up within milliseconds without the user
 * having to restart anything.
 */
class SettingsViewModel(
    private val reelRepository: ReelRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        reelRepository.observeAllTimeTotal(),
    ) { settings, total ->
        SettingsUiState(settings = settings, totalEverCounted = total)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = SettingsUiState(),
    )

    fun setTracking(packageName: String, enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setTrackingFor(packageName, enabled)
    }

    fun setDailyLimit(limit: Int) = viewModelScope.launch {
        settingsRepository.setDailyLimit(limit)
    }

    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setNotificationsEnabled(enabled)
    }

    fun setDebugLogging(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDebugLogging(enabled)
    }

    /** Wipes every recorded reel. There is no undo, hence the dialog in the UI. */
    fun resetAllData() = viewModelScope.launch {
        reelRepository.clearAll()
        settingsRepository.clearAlertHistory()
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
