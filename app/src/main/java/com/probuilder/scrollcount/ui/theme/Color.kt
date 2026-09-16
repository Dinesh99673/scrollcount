package com.probuilder.scrollcount.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ScrollCount's brand palette: a deep violet for the app itself, plus one
 * colour per tracked app so bars and dots are recognisable at a glance.
 *
 * On Android 12+ the user's wallpaper colours replace most of these; the app
 * colours below are always used, because they carry meaning.
 */

// Light scheme
val Violet40 = Color(0xFF5B3FD6)
val VioletContainerLight = Color(0xFFE6DEFF)
val OnVioletContainerLight = Color(0xFF1B0E52)
val Teal40 = Color(0xFF00696E)
val TealContainerLight = Color(0xFF9CF0F5)
val OnTealContainerLight = Color(0xFF002022)
val Coral40 = Color(0xFFB3261E)
val SurfaceLight = Color(0xFFFDFBFF)
val OnSurfaceLight = Color(0xFF1B1B21)
val SurfaceVariantLight = Color(0xFFE5E1EC)
val OnSurfaceVariantLight = Color(0xFF47464F)

// Dark scheme
val Violet80 = Color(0xFFC7BCFF)
val VioletContainerDark = Color(0xFF432EA8)
val OnVioletContainerDark = Color(0xFFE6DEFF)
val Teal80 = Color(0xFF80D4D9)
val TealContainerDark = Color(0xFF004F53)
val OnTealContainerDark = Color(0xFF9CF0F5)
val Coral80 = Color(0xFFFFB4AB)
val SurfaceDark = Color(0xFF131218)
val OnSurfaceDark = Color(0xFFE5E1E9)
val SurfaceVariantDark = Color(0xFF47464F)
val OnSurfaceVariantDark = Color(0xFFC9C5D0)

/** Per-app accent colours, used by charts, dots and the share card. */
val InstagramAccent = Color(0xFFD9337B)
val YouTubeAccent = Color(0xFFE23B2E)
val UnknownAppAccent = Color(0xFF8E8E93)
