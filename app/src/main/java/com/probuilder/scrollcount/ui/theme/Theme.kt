package com.probuilder.scrollcount.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.probuilder.scrollcount.data.TrackedApps

private val LightColors = lightColorScheme(
    primary = Violet40,
    onPrimary = Color.White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = OnVioletContainerLight,
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = TealContainerLight,
    onSecondaryContainer = OnTealContainerLight,
    error = Coral40,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
)

private val DarkColors = darkColorScheme(
    primary = Violet80,
    onPrimary = Color(0xFF2A1276),
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = OnVioletContainerDark,
    secondary = Teal80,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = OnTealContainerDark,
    error = Coral80,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
)

/**
 * Wraps the whole app. Uses the wallpaper-based colours on Android 12 and
 * newer, and the hand-picked violet palette everywhere else.
 */
@Composable
fun ScrollCountTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScrollCountTypography,
        content = content,
    )
}

/** The accent colour for a tracked app, used by charts and per-app rows. */
fun accentFor(packageName: String): Color = when (packageName) {
    TrackedApps.INSTAGRAM -> InstagramAccent
    TrackedApps.YOUTUBE -> YouTubeAccent
    else -> UnknownAppAccent
}
