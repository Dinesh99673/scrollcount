# ScrollCount: Step-by-Step Build Plan

A complete roadmap for building an Android app that counts Instagram Reels and YouTube Shorts, using Kotlin and Claude Code. Each step includes what to build, a prompt you can give Claude Code, and how to know it's done.

## Project details

| | |
|---|---|
| **App name** | ScrollCount |
| **Package name** | `com.probuilder.scrollcount` (permanent, can never change) |
| **Developer name on Play Store** | ProBuilder |
| **Play Console account type** | Personal |
| **Logo** | To be decided (use a placeholder until Phase 6) |

## Timeline at a glance

| Phase | What you build | Time |
|---|---|---|
| 0 | Setup: tools, project, phone | Day 1 |
| 1 | Reel detection (the core) | Days 2–4 |
| 2 | Saving data with Room | Days 5–6 |
| 3 | Screens: onboarding, home, stats, settings | Days 7–9 |
| 4 | Daily limits and notifications | Day 10 |
| 5 | Shareable stats card | Day 11 |
| 6 | Polish, privacy policy, release build | Days 12–13 |
| 7 | Play Store: closed test (14 days), review, launch | Days 14–35 |

**Golden rule:** build one small piece, test it on your real phone, commit to Git, then move on.

---

## Phase 0: Setup (Day 1)

### Step 0.1: Install tools
- Install **Android Studio** (latest stable version).
- Install **Git** and create a free **GitHub** account.
- Install **Claude Code** by following the official setup guide.

### Step 0.2: Prepare your phone
1. Open **Settings → About phone** and tap **Build number** 7 times to unlock Developer Options.
2. Go to **Developer Options** and turn on **USB debugging**.
3. Connect the phone to your computer with a USB cable and allow debugging when asked.
4. Make sure Instagram and YouTube are installed and logged in.

Always test on a real phone. The emulator doesn't have Instagram or YouTube set up the way real users do.

### Step 0.3: Create the project
In Android Studio, choose **New Project → Empty Activity** (this uses Jetpack Compose):
- **Name:** ScrollCount
- **Package name:** `com.probuilder.scrollcount` (this can never change after publishing)
- **Language:** Kotlin
- **Minimum SDK:** API 26 (Android 8.0)
- **Build language:** Kotlin DSL

Open `app/build.gradle.kts` and confirm both values match exactly:
```kotlin
android {
    namespace = "com.probuilder.scrollcount"
    defaultConfig {
        applicationId = "com.probuilder.scrollcount"
    }
}
```

Run the empty app on your phone once to confirm everything works.

### Step 0.4: Set up Git
```bash
git init
git add .
git commit -m "Initial project"
```
Create a GitHub repository and push the code.

### Step 0.5: Create a CLAUDE.md file
Create `CLAUDE.md` in the project root. Claude Code reads it to understand your project every session.

```markdown
# ScrollCount

Android app that counts how many Instagram Reels and YouTube Shorts the user watches.

- App name: ScrollCount
- Package name / applicationId: com.probuilder.scrollcount (never change this)
- Publisher: ProBuilder

## Tech stack
- Kotlin, Jetpack Compose, Material 3
- AccessibilityService for detection
- Room for storage, DataStore for settings
- WorkManager for scheduled tasks
- Vico for charts
- MVVM: ViewModel + StateFlow, manual dependency injection (AppContainer)

## Rules
- All data stays on the device. No internet permission, no analytics, no ads.
- Never read or store text, usernames, or video content from other apps. Only count swipes.
- Each supported app has its own ReelDetector class.
- Explain what each new file does in a short comment at the top.
- Keep functions small and beginner-readable.
```

The "no internet permission" rule makes your privacy promise easy to prove and simplifies the Play Store Data Safety form.

**Done when:** the empty app runs on your phone and the code is on GitHub.

---

## Phase 1: Reel detection (Days 2–4)

This is the hardest and most important phase. Take your time here.

### Step 1.1: Plan the folder structure
```
app/src/main/java/com/probuilder/scrollcount/
├── service/      ScrollCountService.kt
├── detection/    ReelDetector.kt, InstagramDetector.kt, YouTubeDetector.kt
├── data/         database, DAO, repository, settings
├── ui/           onboarding, home, stats, settings, theme
├── notifications/
└── work/         WorkManager jobs
```

### Step 1.2: Create the Accessibility Service skeleton
**Prompt for Claude Code:**
> Create an AccessibilityService called ScrollCountService. Register it in AndroidManifest.xml with the BIND_ACCESSIBILITY_SERVICE permission. Create res/xml/accessibility_service_config.xml that listens only to com.instagram.android and com.google.android.youtube, for event types typeViewScrolled, typeWindowStateChanged, and typeWindowContentChanged, with canRetrieveWindowContent set to true and flagReportViewIds enabled. Add a description string explaining that the app only counts reels and never reads content. For now, just log each event's package name, event type, class name, and source view ID to Logcat with the tag "ScrollDebug". Explain each part to me.

**Test:** Install the app, enable it in **Settings → Accessibility → ScrollCount**, open Instagram, and watch Logcat (filter by `ScrollDebug`). You should see events appear as you scroll.

### Step 1.3: Explore how each app's screens look
Before writing counting logic, you need to learn what makes the Reels screen different from the normal feed.

**Prompt for Claude Code:**
> Add a debug function that, when a scroll event happens, prints the view tree of the current window to Logcat: view ID, class name, and whether it's scrollable, indented by depth. Only run it in debug builds.

**Then do this on your phone:**
1. Scroll the normal Instagram feed and save the log.
2. Open Reels and swipe a few times and save the log.
3. Open Stories and save the log.
4. Repeat for the YouTube home feed and YouTube Shorts.

Compare the logs. Look for a view ID that only appears on the Reels/Shorts screen. Instagram internally calls reels "clips," and YouTube uses words like "reel" or "shorts" in its view IDs, but always confirm with your own logs because these change between app versions.

Paste the logs into Claude Code and ask it to help you spot the unique identifiers.

### Step 1.4: Build the detector design
Use one detector per app. When Instagram updates its layout, you only fix one file.

```kotlin
interface ReelDetector {
    val packageName: String
    val appName: String

    // True if the user is currently on the Reels/Shorts screen
    fun isOnReelScreen(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Boolean

    // True if this event means a new reel came on screen
    fun isNewReel(event: AccessibilityEvent): Boolean
}
```

**Prompt for Claude Code:**
> Create the ReelDetector interface and an InstagramDetector implementation using these view IDs I found in my logs: [paste IDs]. In ScrollCountService, route events to the right detector by package name. Add a debounce so a new reel is counted at most once every 700ms. For now, keep a simple in-memory counter and log "Reel counted: Instagram, total = X".

### Step 1.5: Decide what counts as "watched"
Pick a clear rule and stick to it. Recommended for version 1:
- Count each time a **new reel appears** on screen after a swipe.
- Ignore swipes that happen within 700ms of the last one (duplicate events).
- Scrolling back up to a previous reel still counts. Fixing that is a version 2 feature.

### Step 1.6: Add YouTube Shorts
Repeat Steps 1.3–1.4 for YouTube using a `YouTubeDetector`.

### Step 1.7: Test detection thoroughly
Use this checklist and write down results:
- [ ] Swipe 20 reels on Instagram. Count should be 20 (±1).
- [ ] Swipe 20 Shorts on YouTube. Count should be 20 (±1).
- [ ] Scroll the normal Instagram feed. Count should not change.
- [ ] Watch Instagram Stories. Count should not change.
- [ ] Scroll YouTube home and comments. Count should not change.
- [ ] Open a reel from a DM or a profile. It should still count correctly.
- [ ] Lock and unlock the phone mid-session. Counting continues.
- [ ] Swipe very fast. No double counting.

If a test fails, copy the Logcat output and give it to Claude Code: "This test failed, here are the logs, find the cause."

**Done when:** all checklist items pass. Commit: `git commit -m "Reel detection working for Instagram and YouTube"`.

---

## Phase 2: Saving data (Days 5–6)

### Step 2.1: Create the database
Store each reel as a separate row with a timestamp. This lets you calculate daily, weekly, and hourly stats later.

**Prompt for Claude Code:**
> Add Room to the project. Create an entity ReelEvent with id (auto-generated), appPackage (String), and timestamp (Long, epoch millis). Create a DAO with: insert, a Flow of today's count per app, a Flow of daily totals for the last 7 days, a Flow of counts per hour for today, and deleteAll. Use the device's local time zone for day boundaries. Create a ReelRepository and an AppContainer class that provides the database and repository as singletons.

### Step 2.2: Connect the service to the database
**Prompt for Claude Code:**
> Replace the in-memory counter in ScrollCountService with repository.insert(). Use a CoroutineScope with SupervisorJob and Dispatchers.IO, and cancel it in onDestroy.

### Step 2.3: Add settings storage
**Prompt for Claude Code:**
> Add DataStore Preferences for settings: trackInstagram (default true), trackYouTube (default true), dailyLimit (default 100), notificationsEnabled (default true), onboardingComplete (default false). The service should skip apps that are turned off.

### Test
- [ ] Swipe 10 reels, force-close ScrollCount, reopen. Data is still there.
- [ ] Restart the phone. Old data remains, and counting resumes once the service is running.
- [ ] Turn off YouTube tracking. Shorts are no longer counted.

**Done when:** counts survive app restarts and phone reboots. Commit.

---

## Phase 3: Screens (Days 7–9)

### Step 3.1: Onboarding and permission screen
This screen matters for Play Store approval. It must clearly explain the Accessibility permission **before** sending the user to settings.

**Prompt for Claude Code:**
> Create an onboarding flow in Compose with 3 pages: (1) welcome and what the app does, (2) a clear privacy disclosure stating the app uses the Accessibility Service only to detect when a new reel or short appears, that it does not read messages, usernames, or video content, and that all data stays on the phone with no internet access, (3) a button that opens Settings.ACTION_ACCESSIBILITY_SETTINGS. When the user returns, check whether ScrollCountService is enabled and show a success or retry state. Also request the POST_NOTIFICATIONS permission on Android 13+.

**Note for testers:** On Android 13 and newer, if someone installs your APK file directly (not from Play Store), the Accessibility toggle may be greyed out as a "restricted setting." They can fix it in **Settings → Apps → ScrollCount → ⋮ menu → Allow restricted settings**. Installing through Play Store closed testing avoids this.

### Step 3.2: Home screen
**Prompt for Claude Code:**
> Create a HomeScreen with a HomeViewModel exposing StateFlow. Show: today's total reels in large text, a breakdown per app with icons, estimated time spent (count × 20 seconds, clearly labeled "estimated"), progress toward the daily limit as a progress bar, and a warning banner with a fix button if the Accessibility Service is turned off.

### Step 3.3: Stats screen
**Prompt for Claude Code:**
> Add the Vico chart library. Create a StatsScreen with a bar chart of the last 7 days, a chart of reels per hour today, and summary text: weekly total, daily average, and busiest day.

### Step 3.4: Settings screen
**Prompt for Claude Code:**
> Create a SettingsScreen with toggles for each tracked app, a daily limit selector, a notifications toggle, a "Reset all data" button with a confirmation dialog, and a link to the privacy policy.

### Step 3.5: Navigation and theme
**Prompt for Claude Code:**
> Add Navigation Compose with a bottom bar for Home, Stats, and Settings. Show onboarding first if onboardingComplete is false. Use Material 3 with dynamic colors and support dark mode.

### Test
- [ ] Fresh install shows onboarding; later launches go straight to Home.
- [ ] Home count updates live while you swipe reels in another app and come back.
- [ ] Charts show correct numbers after a few days of use.
- [ ] Turning off the service shows the warning banner.
- [ ] Everything looks right in both light and dark mode.

**Done when:** a friend can install and set up the app without your help. Commit.

---

## Phase 4: Limits and notifications (Day 10)

**Prompt for Claude Code:**
> Create a notification channel. When today's count reaches 50% and 100% of the daily limit, send one notification each, only once per day per threshold (store which ones were sent in DataStore). Add a WorkManager periodic job that sends a daily summary notification around 9 PM with today's count compared to yesterday. Respect the notificationsEnabled setting.

Keep version 1 to notifications only. Blocking apps or drawing overlays requires more permissions and makes Play Store review harder.

### Test
- [ ] Set the limit to 5, swipe 5 reels, and receive the alert once.
- [ ] Keep swiping; no repeated alerts.
- [ ] Daily summary arrives in the evening.

**Done when:** alerts are helpful and never spammy. Commit.

---

## Phase 5: Shareable stats card (Day 11)

This is your built-in marketing feature.

**Prompt for Claude Code:**
> Create a share feature on the Stats screen. Design a Compose card (1080×1920, story-sized) showing the weekly reel count, estimated hours, a short fun line like "That's about X movies worth of scrolling," and "ScrollCount" with a placeholder logo at the bottom. Render it to a Bitmap, save it in the cache folder, and share it with Intent.ACTION_SEND using a FileProvider.

### Test
- [ ] Share to Instagram Stories and WhatsApp. The image looks sharp and correct.

**Done when:** you'd actually want to post it. Commit.

---

## Phase 6: Polish and release build (Days 12–13)

### Step 6.1: Reliability
- Some phone brands (Xiaomi, Oppo, Vivo, Realme, Samsung) aggressively stop background apps. Add a help screen explaining how to disable battery optimization for ScrollCount.
- Test on at least 2–3 different phones if you can borrow them.

### Step 6.2: Visual polish
- **App icon:** keep Android Studio's default icon until your ScrollCount logo is ready. When it is, add it with **File → New → Image Asset** (adaptive icon, with a transparent foreground layer), and update the share card too. The final logo must be in place before Phase 7.
- Empty states, such as "No reels yet today 🎉".
- Check text sizes and spacing on a small phone.

### Step 6.3: Privacy policy
Play Store requires one. Write a simple page stating what the app detects, that nothing is collected or shared, and that no data leaves the device. Host it free on **GitHub Pages**.

**Prompt for Claude Code:**
> Write a simple, honest privacy policy for this app based on what the code actually does, as a Markdown file I can host on GitHub Pages.

### Step 6.4: Release build
**Prompt for Claude Code:**
> Set up a release build: enable R8 minification with the ProGuard rules needed for Room and the other libraries, set the latest target SDK, and explain how to generate a signed Android App Bundle (.aab).

In Android Studio: **Build → Generate Signed Bundle / APK → Android App Bundle**, and create a new keystore.

**Back up your keystore file and passwords in two safe places.** If you lose it, updating your app becomes very difficult.

### Test
- [ ] Install the release build and repeat the Phase 1 detection checklist. Minification sometimes breaks things.

**Done when:** the release build works exactly like the debug build. Commit and tag: `git tag v1.0.0`.

---

## Phase 7: Play Store launch (Days 14–35)

### Step 7.1: Create your developer account (start early)
Register at the Google Play Console, pay the one-time fee, and complete identity verification. Verification can take a few days, so you can do this during Phase 1.
- **Account type:** Personal
- **Developer name:** ProBuilder
- **Email:** use a separate email just for the developer account, since it's shown publicly
- Google also shows your legal name and country publicly on personal accounts

### Step 7.1b: Lock in your package name early
As soon as your account is verified and you have any working build (even from Phase 1), create the app **ScrollCount** in Play Console and upload a signed bundle to **internal testing**. This permanently reserves `com.probuilder.scrollcount` for you, so no one else can take it while you build.

Internal testing doesn't count toward the 14-day closed test, so you'll still do Step 7.4 later.

### Step 7.2: Prepare the store listing
- **App name:** include a keyword, e.g., "ScrollCount: Doomscroll Tracker"
- **Short description (80 chars):** e.g., "Count the reels & shorts you watch. Take back control of your scrolling."
- **Full description:** features, privacy promise, how it works
- **Graphics:** 512×512 icon (your final ScrollCount logo), 1024×500 feature graphic, 4–8 phone screenshots

### Step 7.3: Complete the App Content section
- **Privacy policy:** your GitHub Pages link
- **Data safety:** declare that no data is collected or shared (true if you kept the no-internet rule)
- **Accessibility API declaration:** explain that the app detects reel swipes for digital wellbeing and nothing else. Prepare a short screen recording showing your disclosure screen and the counter working, since Google may ask for one.
- **Content rating, target audience, ads declaration:** fill in honestly

Do not mark the app as an accessibility tool for people with disabilities. It's a digital wellbeing app, and mislabeling it can get it rejected.

### Step 7.4: Run the closed test
1. Create a **closed testing** track and upload your .aab.
2. Add at least 15 testers (a few extra in case some drop out).
3. Share the opt-in link; each tester must join and install from Play Store.
4. Keep them opted in for **14 continuous days**.
5. Ask testers to actually use the app daily and send feedback.
6. Release 1–2 updates during the test based on feedback. This shows Google real engagement.

### Step 7.5: Apply for production
After 14 days, apply for production access in the Play Console dashboard. You'll answer questions about your testing and what you changed. Review usually takes a few days.

### Step 7.6: Launch
- Post the share card and a short demo reel.
- Share on Reddit, LinkedIn, college groups, and GitHub.
- Reply to every early review.

---

## Version 2 ideas (after launch)
- Support Facebook Reels and Snapchat Spotlight
- Home screen widget showing today's count
- Don't count scrolling back to a previous reel
- Weekly streaks and goals ("under 50 reels a day for 7 days")
- Hourly heatmap showing when you scroll most
- Export your data as CSV

## Ongoing maintenance
Instagram and YouTube update often, which can break detection. Plan to:
- Check detection every time either app updates on your phone.
- Keep the Phase 1 debug logging in debug builds so you can quickly find new view IDs.
- Watch for a sudden drop in counts in your own usage, since that usually means a layout changed.
