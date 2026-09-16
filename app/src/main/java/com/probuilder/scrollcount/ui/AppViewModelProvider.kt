package com.probuilder.scrollcount.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.probuilder.scrollcount.ScrollCountApp
import com.probuilder.scrollcount.di.AppContainer
import com.probuilder.scrollcount.ui.home.HomeViewModel
import com.probuilder.scrollcount.ui.onboarding.OnboardingViewModel
import com.probuilder.scrollcount.ui.settings.SettingsViewModel
import com.probuilder.scrollcount.ui.stats.StatsViewModel

/**
 * Builds every ViewModel in the app.
 *
 * Because dependencies are wired by hand rather than by a framework, each
 * ViewModel needs somewhere to get its repositories from. This factory pulls
 * them out of the Application's AppContainer and hands them over.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val container = container()
            HomeViewModel(container.reelRepository, container.settingsRepository)
        }
        initializer {
            val container = container()
            StatsViewModel(container.reelRepository, container.settingsRepository)
        }
        initializer {
            val container = container()
            SettingsViewModel(container.reelRepository, container.settingsRepository)
        }
        initializer {
            OnboardingViewModel(container().settingsRepository)
        }
    }
}

private fun CreationExtras.container(): AppContainer {
    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
    return (application as ScrollCountApp).container
}
