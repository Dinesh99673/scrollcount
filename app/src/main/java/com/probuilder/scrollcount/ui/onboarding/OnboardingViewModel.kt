package com.probuilder.scrollcount.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.probuilder.scrollcount.data.settings.SettingsRepository
import kotlinx.coroutines.launch

/**
 * Holds the one piece of state onboarding needs to change: whether the user has
 * been through it. Everything else on those screens is presentation only.
 */
class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    fun finish(onDone: () -> Unit) = viewModelScope.launch {
        settingsRepository.setOnboardingComplete(true)
        onDone()
    }
}
