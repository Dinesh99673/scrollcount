package com.probuilder.scrollcount.detection

/**
 * Looks up the right detector for whichever app sent an event.
 *
 * Adding support for a new app (Facebook Reels, Snapchat Spotlight) is two
 * steps: write a detector, add it to the list below, then add its package name
 * to res/xml/accessibility_service_config.xml.
 */
class DetectorRegistry(
    private val detectors: List<ReelDetector> = listOf(
        InstagramDetector(),
        YouTubeDetector(),
    ),
) {
    fun forPackage(packageName: String): ReelDetector? =
        detectors.firstOrNull { it.packageName == packageName }

    fun resetAll() = detectors.forEach { it.onLeftReelScreen() }
}
