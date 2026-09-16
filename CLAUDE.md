# ScrollCount

Android app that counts how many Instagram Reels and YouTube Shorts the user watches.

- App name: ScrollCount
- Package name / applicationId: com.probuilder.scrollcount (never change this)
- Publisher: ProBuilder

## Tech stack
- Kotlin, Jetpack Compose, Material 3
- AccessibilityService for detection
- Room for storage, DataStore for settings
- WorkManager for the daily summary notification
- Charts are hand-built Compose layout (`ui/components/BarChart.kt`), not a chart library
- MVVM: ViewModel + StateFlow, manual dependency injection (AppContainer)

## Rules
- All data stays on the device. No internet permission, no analytics, no ads.
  The manifest explicitly removes `android.permission.INTERNET` so no library can add it.
- Never read or store text, usernames, or video content from other apps. Only count swipes.
- Each supported app has its own ReelDetector class.
- Explain what each new file does in a short comment at the top.
- Keep functions small and beginner-readable.

## Layout
```
app/src/main/java/com/probuilder/scrollcount/
├── service/        ScrollCountService, service state, permission helpers
├── detection/      ReelDetector interface, per-app detectors, debug view-tree logger
├── data/           Room database + DAO, ReelRepository, DataStore settings
├── di/             AppContainer (manual DI)
├── notifications/  ReelNotifier
├── work/           DailySummaryWorker + scheduler
├── ui/             onboarding, home, stats, settings, help, share, theme, navigation
└── util/           TimeRanges, Formatting, Links
```

## Build
```
./gradlew :app:assembleDebug      # debug APK
./gradlew :app:assembleRelease    # minified release (needs signing config for upload)
./gradlew :app:installDebug       # install on the connected phone
```
`local.properties` must contain `sdk.dir=` with forward slashes.

## When detection breaks after an Instagram or YouTube update
1. Install the debug build, enable **Settings → Developer → Log view trees**.
2. `adb logcat -s ScrollDebug`
3. Open the reel screen, swipe, and compare the dump against the normal feed.
4. Find a view ID present only on the reel screen and add it to the matching
   detector's `screenMarkers` list in `detection/`.

Instagram calls Reels "clips" internally and uses "reel" for Stories — match on
"clips". YouTube calls Shorts "reel".
