package com.probuilder.scrollcount.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.probuilder.scrollcount.ui.help.BatteryHelpScreen
import com.probuilder.scrollcount.ui.home.HomeScreen
import com.probuilder.scrollcount.ui.onboarding.OnboardingScreen
import com.probuilder.scrollcount.ui.settings.SettingsScreen
import com.probuilder.scrollcount.ui.stats.StatsScreen

/**
 * The app's single navigation graph.
 *
 * Onboarding is part of the same graph rather than a separate activity, which
 * means finishing it is just a navigation that removes it from the back stack -
 * so pressing back afterwards leaves the app instead of returning to the
 * welcome screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollCountNavHost(
    startWithOnboarding: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute != Routes.ONBOARDING
    val canGoBack = currentRoute == Routes.HELP

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showChrome) {
                TopAppBar(
                    title = { Text(titleForRoute(currentRoute)) },
                    navigationIcon = {
                        if (canGoBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showChrome && !canGoBack) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigateToTab(navController, destination.route) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startWithOnboarding) Routes.ONBOARDING else Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onOpenHelp = { navController.navigate(Routes.HELP) })
            }
            composable(Routes.HELP) { BatteryHelpScreen() }
        }
    }
}

/**
 * Switching tabs should never stack screens up. This pops back to the start of
 * the graph first, and restores whatever state that tab had.
 */
private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
