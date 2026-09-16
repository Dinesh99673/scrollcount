# ScrollCount

An Android app that counts the Instagram Reels and YouTube Shorts you watch,
and keeps every number on your own phone.

- **Package name:** `com.probuilder.scrollcount` (permanent — never change it)
- **Publisher:** ProBuilder
- **Minimum Android:** 8.0 (API 26) · **Target:** API 36

---

## What it does

| Screen | What it shows |
|---|---|
| **Onboarding** | What the app is, exactly what the accessibility permission is used for, and the button to switch it on |
| **Home** | Today's count, a per-app breakdown, estimated time spent, and progress toward your daily limit |
| **Stats** | Last 7 days, today by hour, weekly summary, and a shareable story-sized card |
| **Settings** | Which apps to count, daily limit, notifications, the battery fix-it guide, and "Reset all data" |

It also sends at most three notifications a day: one at half your limit, one when
you reach it, and an evening summary around 9 PM.

## Privacy, in one paragraph

The accessibility service is restricted to Instagram and YouTube. Inside them it
checks one thing — whether the full-screen reel player is showing and whether a
different reel has appeared. Each reel becomes one database row holding the app's
package name and a timestamp, and nothing else. The manifest **removes** the
`INTERNET` permission, so the app cannot transmit anything even in principle, and
cloud backup is switched off.

---

## Running it

### First-time setup

1. `local.properties` needs your SDK path, with **forward slashes**:
   ```properties
   sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
   ```
2. Connect a real phone with USB debugging on. (The emulator has no real
   Instagram or YouTube usage to detect.)
3. Build and install:
   ```bash
   ./gradlew :app:installDebug
   ```
4. On the phone: **Settings → Accessibility → ScrollCount → On**.

> **Debug builds install as `com.probuilder.scrollcount.debug`**, so they sit
> alongside a release build without clashing.

> **If the accessibility toggle is greyed out** after installing an APK directly
> (Android 13+ calls this a "restricted setting"): **Settings → Apps →
> ScrollCount → ⋮ → Allow restricted settings**. Installing through Play Store
> testing avoids this entirely.

### Useful commands

```bash
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:installDebug         # build + install
./gradlew :app:assembleRelease      # minified release build
./gradlew :app:bundleRelease        # .aab for Play Store
adb logcat -s ScrollDebug           # watch detection while you scroll
```

---

## Detection test checklist

Run this on a real phone after any change to `detection/`, and again on the
release build (R8 can break things the debug build hides).

- [ ] Swipe 20 reels on Instagram → count goes up by 20 (±1)
- [ ] Swipe 20 Shorts on YouTube → count goes up by 20 (±1)
- [ ] Scroll the normal Instagram feed → count does not change
- [ ] Watch Instagram Stories → count does not change
- [ ] Scroll YouTube home and a video's comments → count does not change
- [ ] Open a reel from a DM or a profile → still counts
- [ ] Lock and unlock the phone mid-session → counting continues
- [ ] Swipe very fast → no double counting
- [ ] Turn YouTube off in Settings → Shorts stop being counted
- [ ] Force-close the app and reopen → today's count is still there
- [ ] Reboot the phone → old data intact, counting resumes

### When an app update breaks counting

Instagram and YouTube change their layouts regularly. The fix is always in one
file.

1. Install the debug build and turn on **Settings → Developer → Log view trees**.
2. `adb logcat -s ScrollDebug`
3. Dump the view tree three times: the normal feed, the reel player, and stories.
4. Find a view ID that appears **only** in the reel player.
5. Add it to `screenMarkers` in `detection/InstagramDetector.kt` or
   `detection/YouTubeDetector.kt`.

Naming trap worth remembering: Instagram calls Reels **"clips"** and uses
**"reel"** for Stories. YouTube calls Shorts **"reel"**.

---

## Releasing

### 1. Create a keystore (once, ever)

```bash
keytool -genkey -v -keystore scrollcount-release.jks -keyalg RSA \
  -keysize 2048 -validity 10000 -alias scrollcount
```

### 2. Point the build at it

Create `keystore.properties` in the project root — it is git-ignored:

```properties
storeFile=C:/path/to/scrollcount-release.jks
storePassword=...
keyAlias=scrollcount
keyPassword=...
```

If this file is missing the release build still compiles; it just comes out
unsigned, which is fine for testing minification.

### 3. Build the bundle

```bash
./gradlew :app:bundleRelease
# app/build/outputs/bundle/release/app-release.aab
```

> **Back up the `.jks` file and both passwords in two separate safe places.**
> Lose them and you can never publish an update to this app again.

### 4. Privacy policy

`docs/privacy-policy.md` is ready to host on GitHub Pages: repository
**Settings → Pages → Source: main branch, /docs folder**. Replace the contact
email placeholder first, then put the resulting URL into
`util/Links.kt` and the Play Console listing.

---

## Play Store checklist

- [ ] Developer account verified (personal, "ProBuilder"), using a **separate**
      public email
- [ ] App created in Play Console and a build uploaded to **internal testing** —
      this reserves the package name
- [ ] Final logo replaces the placeholder chevron icon (`File → New → Image
      Asset`), and the share card footer updated to match
- [ ] Store listing: name with a keyword, 80-char short description, full
      description, 512×512 icon, 1024×500 feature graphic, 4–8 screenshots
- [ ] Data safety form: **no data collected, no data shared** (true as long as
      the no-internet rule holds)
- [ ] Accessibility API declaration: describe it as a digital wellbeing app that
      detects reel swipes. **Do not** label it an accessibility tool for people
      with disabilities — that is a rejection risk
- [ ] Screen recording ready showing the disclosure screen and the counter
      working, in case Google asks
- [ ] Closed test with 15+ testers, opted in for 14 continuous days, with 1–2
      updates shipped during it
- [ ] Apply for production access

---

## Ideas for version 2

Facebook Reels and Snapchat Spotlight · home screen widget · not counting
scroll-backs · streaks and goals · hourly heatmap · CSV export.
