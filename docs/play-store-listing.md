# Play Store listing — draft copy

Ready to paste into Play Console. Adjust the wording to sound like you.

## App name (30 characters max)

```
ScrollCount: Reel Counter
```

Alternatives if that one is taken:
- `ScrollCount - Doomscroll Log` (28)
- `ScrollCount: Shorts Counter` (27)

## Short description (80 characters max)

```
Count the reels and shorts you watch. All offline. Nothing ever leaves phone.
```

(76 characters)

## Full description (4000 characters max)

```
How many Reels did you watch today? Most people have no idea. ScrollCount gives
you the number.

It sits quietly in the background and counts each Instagram Reel and YouTube
Short that appears on your screen. No blocking, no shaming, no streak you have to
maintain. Just an honest count, and what you do with it is your business.

WHAT YOU GET

• Today's count, the moment you open the app
• A breakdown by app, so you know where the time actually goes
• Last 7 days as a chart, plus your busiest day and your busiest hour
• A daily limit you set yourself, with one nudge at halfway and one when you
  reach it
• An optional evening summary comparing today with yesterday
• A shareable card of your week, if you are brave enough to post it

BUILT TO BE PRIVATE

ScrollCount has no internet permission. Not "we promise not to use it" - the
permission is removed from the app, so it is technically incapable of sending
anything anywhere.

• No account, no sign-up, no email
• No analytics, no advertising, no tracking libraries
• Nothing is uploaded, because nothing can be
• Delete everything with one button, any time

ABOUT THE ACCESSIBILITY PERMISSION

To notice when a new reel appears, ScrollCount uses Android's Accessibility
Service. It is limited to two apps - Instagram and YouTube - and inside them it
checks one thing: whether the full-screen reel player is showing, and whether a
different reel has appeared since it last looked.

It does not read your messages, comments, captions, usernames or searches. It
does not record video or audio. It does not know which reel you watched, or who
made it. Every reel becomes one line in a database on your phone: which app, and
what time. That is all.

HONEST LIMITATIONS

• The "time spent" figure is an estimate based on about 20 seconds per reel.
  ScrollCount counts reels, it does not time them.
• Scrolling back up to a reel you already saw counts again.
• Instagram and YouTube change their layouts often. If counting ever stops,
  update the app - fixing detection is the main thing updates do.
• Some phones aggressively shut down background apps. The app includes a guide
  for Xiaomi, Oppo, Vivo, Realme, OnePlus and Samsung devices.

Made by ProBuilder.
```

## Graphics checklist

| Asset | Size | Status |
|---|---|---|
| App icon | 512 × 512 PNG, 32-bit | Replace placeholder chevron first |
| Feature graphic | 1024 × 500 PNG/JPG | Needed |
| Phone screenshots | 4–8, min 320px, 16:9 or 9:16 | Home, Stats, Onboarding privacy page, Settings, share card |

Screenshot tip: take them **after** a few days of real use so the charts have
real data in them.

## App content answers

| Question | Answer |
|---|---|
| Privacy policy URL | Your GitHub Pages link to `docs/privacy-policy.md` |
| Data collection | **No data collected** |
| Data sharing | **No data shared** |
| Data encrypted in transit | N/A — no data leaves the device |
| Ads | No |
| In-app purchases | No |
| Target audience | 13+ |
| Is it an accessibility tool? | **No** — it is a digital wellbeing app |

## Accessibility API declaration

Paste something close to this:

```
ScrollCount is a digital wellbeing app that shows users how many short-form
videos (Instagram Reels and YouTube Shorts) they have watched.

The AccessibilityService is used solely to detect when a new full-screen reel or
short appears on screen, by checking whether the current window contains the view
IDs belonging to those apps' full-screen players. When it detects a new one, the
app increments a local counter.

The service is restricted via android:packageNames to com.instagram.android and
com.google.android.youtube only. It does not read text content, user input,
messages, usernames or media. No data is transmitted: the app does not declare
the INTERNET permission, and it is explicitly removed during the manifest merge.

Users are shown a full-screen in-app disclosure explaining this before they are
sent to the accessibility settings, and can disable the service at any time.
```

Have a 30–60 second screen recording ready showing the disclosure screen, the
permission being granted, and the counter going up as you swipe.
