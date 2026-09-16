package com.probuilder.scrollcount.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Every screen the app can navigate to, and the three that appear in the
 * bottom bar. Keeping the route strings here stops them being typed by hand in
 * more than one place.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val HELP = "help"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home", Icons.Filled.Home),
    BottomDestination(Routes.STATS, "Stats", Icons.Filled.BarChart),
    BottomDestination(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

/** The title shown in the top bar for a given route. */
fun titleForRoute(route: String?): String = when (route) {
    Routes.STATS -> "Your stats"
    Routes.SETTINGS -> "Settings"
    Routes.HELP -> "Keep it counting"
    else -> "ScrollCount"
}
