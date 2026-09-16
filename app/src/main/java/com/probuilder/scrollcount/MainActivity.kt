package com.probuilder.scrollcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.probuilder.scrollcount.ui.navigation.ScrollCountNavHost
import com.probuilder.scrollcount.ui.theme.ScrollCountTheme
import kotlinx.coroutines.flow.map

/**
 * The app's only activity. Everything else is Compose.
 *
 * Its one job beyond setting up the theme is deciding which screen comes first:
 * onboarding on a fresh install, home every time after that. The decision waits
 * for DataStore to load so the welcome screens never flash up for a returning
 * user.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = (application as ScrollCountApp).container.settingsRepository

        setContent {
            ScrollCountTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val onboardingFlow = remember {
                        settingsRepository.settings.map { it.onboardingComplete }
                    }
                    val onboardingComplete by onboardingFlow
                        .collectAsStateWithLifecycle(initialValue = null)

                    when (val complete = onboardingComplete) {
                        // Settings not read yet - hold a blank surface for a frame
                        // or two rather than guessing wrong.
                        null -> Box(modifier = Modifier.fillMaxSize())
                        else -> ScrollCountNavHost(startWithOnboarding = !complete)
                    }
                }
            }
        }
    }
}
