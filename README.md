# ScrollSense

**ScrollSense** is an Android digital-wellness application built with **Kotlin**, **Jetpack Compose**, and **Material 3**. It is engineered to help users reclaim conscious attention from addictive short-form video algorithms (specifically Instagram Reels and YouTube Shorts).

---

## Table of Contents
1. [What ScrollSense Is](#1-what-scrollsense-is)
2. [How to Open in Android Studio](#2-how-to-open-in-android-studio)
3. [How to Sync Gradle](#3-how-to-sync-gradle)
4. [How to Run the Application](#4-how-to-run-the-application)
5. [How to Install on a Physical Android Phone](#5-how-to-install-on-a-physical-android-phone)
6. [Required Permissions](#6-required-permissions)
7. [How to Enable Usage Access](#7-how-to-enable-usage-access)
8. [How to Enable Accessibility](#8-how-to-enable-accessibility)
9. [How to Test the Limit / Blocking Feature (Test Mode)](#9-how-to-test-the-limit--blocking-feature-test-mode)
10. [How to Use Demo Mode](#10-how-to-use-demo-mode)
11. [What Is Reliably Enforced](#11-what-is-reliably-enforced)
12. [What Is Estimated](#12-what-is-estimated)
13. [Limitations of Exact Reel/Short Detection](#13-limitations-of-exact-reelshort-detection)
14. [Privacy Model](#14-privacy-model)
15. [Known Android Limitations](#15-known-android-limitations)
16. [Project File Structure](#16-project-file-structure)

---

## 1. What ScrollSense Is

ScrollSense is a real, functional digital-wellness Android app designed for everyday use. Rather than relying on simple timers that can be dismissed with a single tap, ScrollSense:
- **Monitors** usage time for Instagram (`com.instagram.android`) and YouTube (`com.google.android.youtube`).
- **Alerts** the user when reaching ~80% of their daily allowance.
- **Intervenes** calmly but firmly when the daily limit is exhausted by returning the device to the Home screen and presenting a dedicated full-screen intervention.
- **Prompts Mindful Reflection**, giving users a way to log their opening trigger (boredom, stress, habit, procrastination) and current mood.
- **Calculates an Attention Score (0–100)** and visualizes a 7-day analytics dashboard.

---

## 2. How to Open in Android Studio

1. Launch **Android Studio** on your Mac (Apple Silicon M1/M2/M3/M4 or Intel).
2. On the Welcome screen, click **Open** (or go to `File > Open...`).
3. Navigate to and select this exact folder:
   ```
   /Users/amansingh/Desktop/Scroll Sense
   ```
4. Click **Open**.

---

## 3. How to Sync Gradle

When the project opens, Android Studio will automatically prompt to sync Gradle.

If you need to manually trigger Gradle Sync:
- Click the **Sync Project with Gradle Files** icon (the small elephant with a downward arrow) in the top-right toolbar.
- Or select from the menu: `File > Sync Project with Gradle Files`.

The project is pre-configured with:
- **Gradle Wrapper**: `8.11.1`
- **Android Gradle Plugin (AGP)**: `8.7.3`
- **Kotlin**: `2.0.21` with Compose Compiler Plugin
- **Min SDK**: `26` (Android 8.0 Oreo)
- **Target / Compile SDK**: `35` (Android 15)
- **Java Compatibility**: JDK 17 / 21

---

## 4. How to Run the Application

1. Open Android Studio's **Device Manager** (`Tools > Device Manager`).
2. Start an Android Virtual Device (AVD) running Android 8.0 or higher (recommended: Android 13, 14, or 15).
3. Ensure the run configuration is set to **`app`** in the top toolbar dropdown.
4. Click the green **Run ▶** button (or press `Shift + F10` / `Control + R`).

---

## 5. How to Install on a Physical Android Phone

1. **Enable Developer Options on your phone**:
   - Go to `Settings > About phone`.
   - Tap `Build number` 7 times until you see *"You are now a developer!"*.
2. **Enable USB Debugging**:
   - Go to `Settings > System > Developer options`.
   - Toggle on **USB debugging**.
3. **Connect phone to your Mac** via USB cable.
   - Accept the *"Allow USB debugging?"* prompt on your phone screen.
4. In Android Studio, select your physical device from the device dropdown and click **Run ▶**.

Alternatively, you can build a debug APK via the Gradle wrapper terminal:
```bash
./gradlew assembleDebug
```
The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
You can transfer and install this APK directly via `adb install app/build/outputs/apk/debug/app-debug.apk`.

---

## 6. Required Permissions

ScrollSense requires only the minimum permissions necessary to function:

| Permission | Technical Name | Purpose |
| :--- | :--- | :--- |
| **Usage Access** | `android.permission.PACKAGE_USAGE_STATS` | Authoritative measurement of foreground time for Instagram & YouTube |
| **Accessibility Service** | `android.permission.BIND_ACCESSIBILITY_SERVICE` | Real-time foreground app detection, scroll heuristics, and limit enforcement |
| **Notifications** | `android.permission.POST_NOTIFICATIONS` | 80% boundary approach warnings (Android 13+) |
| **Boot Completed** | `android.permission.RECEIVE_BOOT_COMPLETED` | Restores alarms and notification channels on reboot |
| **Display Over Other Apps** | `android.permission.SYSTEM_ALERT_WINDOW` | Optional: ensures instant intervention presentation |

---

## 7. How to Enable Usage Access

1. Open **ScrollSense**.
2. Tap the **Protection Needs Setup** banner on the Home screen (or open the **Permissions** / **Settings** screen).
3. Tap **Grant** next to **Usage Access**.
4. Android Settings will open to the *Usage access* list.
5. Find **ScrollSense** in the list and toggle **Permit usage access** to **ON**.
6. Press Back to return to ScrollSense. The status indicator will turn green.

---

## 8. How to Enable Accessibility

1. In ScrollSense, tap **Grant** next to **Accessibility Service**.
2. Android Settings will open to the *Accessibility* screen.
3. Tap **Downloaded apps** (or *Installed services* on Samsung/Pixel devices).
4. Tap **ScrollSense Attention Guard**.
5. Toggle **Use ScrollSense Attention Guard** to **ON**.
6. A system dialog explaining the capability will appear. Tap **Allow**.
7. Return to ScrollSense. The banner will update to **Protection Active**.

---

## 9. How to Test the Limit / Blocking Feature (Test Mode)

You do **not** need to wait a whole day or spend 30 minutes scrolling to test the blocking behavior.

1. In ScrollSense, navigate to the **Limits** tab (or **Settings**).
2. Toggle on **Test Mode**.
3. Select a small limit, such as **1 min** or **3 min**.
4. Launch **Instagram** or **YouTube** on your device.
5. Browse for the configured 1–3 minutes.
6. When the time is elapsed:
   - ScrollSense immediately intervenes and minimizes the protected app (`GLOBAL_ACTION_HOME`).
   - The dedicated full-screen **Limit reached** screen appears.
   - If you attempt to switch back to Instagram/YouTube, ScrollSense immediately blocks re-entry.
7. Tap **Go back** to return home, or **Reflect** to record your opening mood.
8. To re-test immediately, tap **Reset Today's Test Data** in the Limits or Settings tab.

---

## 10. How to Use Demo Mode

If you are evaluating the UI or demonstrating the application without having used it for 7 days:

1. Go to the **Settings** tab.
2. Under **Testing & Demonstration**, toggle on **Demo Mode**.
3. Return to the **Home** or **Analytics** tab:
   - A full 7-day usage history is populated.
   - Attention score displays real computed metrics (~78/100).
   - 7-day stacked bar charts for Instagram and YouTube appear.
   - Limit adherence percentage, late-night hours, and interventions are filled.
   - Reflection triggers (Boredom, Stress, Habit) and mood distribution appear.
4. Toggle Demo Mode **OFF** at any time to return to your real physical device records.

---

## 11. What Is Reliably Enforced

- **Total Foreground Time**: Measured via Android's `UsageStatsManager` with minute precision.
- **Limit Enforcement**: When used minutes $\ge$ configured limit minutes, the app is closed and the intervention screen is displayed.
- **Anti-Bypass Protection**: Repeatedly attempting to reopen the restricted application triggers an immediate re-intervention.
- **80% Warning Notification**: Dispatched as an Android system notification with rate-limiting.
- **Daily Midnight Reset**: Computed dynamically from the current day's start epoch (configurable to 12:00 AM, 4:00 AM, etc.).

---

## 12. What Is Estimated

- **Individual Reel and Short Video Counts**:
  - Estimated by `ScrollSenseAccessibilityService` observing debounced `TYPE_VIEW_SCROLLED` events while inside Reels / Shorts container views.
  - The UI explicitly labels these numbers as **Estimated Reels** and **Estimated Shorts**.
  - **Enforcement is always anchored to reliable foreground usage time**, preventing false blocks due to UI heuristic misses.

---

## 13. Limitations of Exact Reel/Short Detection

1. **No Public Android API**: Neither Android, Meta, nor Google provides an API endpoint or intent broadcast reporting when a short-form video finishes playing.
2. **Frequent Layout Changes**: Third-party apps frequently obfuscate and update view IDs and layout hierarchies in weekly Play Store updates.
3. **Rapid Flicks**: A user rapidly flicking their thumb across 4 videos in 2 seconds is handled with a 1.4-second debounce to avoid over-counting.

*(For a deep-dive on this topic, read [TECHNICAL_ARCHITECTURE.md](TECHNICAL_ARCHITECTURE.md) in the project root).*

---

## 14. Privacy Model

- **Zero Network Permissions**: ScrollSense does **NOT** declare `android.permission.INTERNET`. It is technically incapable of sending any data over the internet.
- **Zero Third-Party Trackers**: No Firebase Analytics, no Facebook SDK, no remote analytics.
- **Strict Accessibility Ethics**: The AccessibilityService inspects only package names, container layout resource IDs, and scroll events for Instagram and YouTube. It **never** inspects passwords, text fields, or private messages.
- **100% Local Storage**: Everything is stored exclusively in Room SQLite and Jetpack DataStore on your local flash memory.

---

## 15. Known Android Limitations

1. **OEM Battery Optimization**:
   - On aggressive OEM skins (e.g., Xiaomi MIUI/HyperOS, OnePlus OxygenOS, Huawei EMUI), the operating system may kill background accessibility services after prolonged sleep.
   - *Workaround*: Disable battery optimization for ScrollSense in your device's battery settings (`Settings > Apps > ScrollSense > Battery > Unrestricted`).
2. **Reboot Accessibility Recovery**:
   - In rare cases on older Android versions, accessibility services must be manually re-toggled after phone restart.
   - ScrollSense registers a `BootReceiver` for `BOOT_COMPLETED` and clearly displays the protection state on the Home dashboard so the user is immediately aware.

---

## 16. Project File Structure

```
ScrollSense/
├── app/
│   ├── build.gradle.kts                 # App build config (SDK 35, Min 26, Compose, Room, KSP)
│   ├── proguard-rules.pro               # Proguard rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Permissions, activities, accessibility service, receivers
│       │   ├── res/
│       │   │   ├── drawable/            # Launcher and vector icons
│       │   │   ├── mipmap-anydpi-v26/   # Adaptive launcher icons
│       │   │   ├── values/              # strings.xml, colors.xml, themes.xml
│       │   │   └── xml/                 # accessibility_service_config.xml, backup rules
│       │   └── java/com/example/scrollsense/
│       │       ├── ScrollSenseApp.kt    # Application class (DI, DB, Notification channels)
│       │       ├── MainActivity.kt      # Main Edge-to-Edge Activity
│       │       ├── data/
│       │       │   ├── local/           # Room Database, Entities, DAOs
│       │       │   ├── preferences/     # Jetpack DataStore UserPreferencesRepository
│       │       │   └── repository/      # UsageRepository (data coordination)
│       │       ├── domain/
│       │       │   ├── model/           # ProtectedApp, DailyAppUsage, AttentionScore, etc.
│       │       │   └── usecase/         # CalculateAttentionScoreUseCase, GenerateInsightsUseCase
│       │       ├── service/
│       │       │   ├── ScrollSenseAccessibilityService.kt # Core detection & enforcement
│       │       │   ├── UsageTrackingManager.kt            # UsageStatsManager wrapper
│       │       │   ├── NotificationHelper.kt              # Notification channel & warnings
│       │       │   ├── BootReceiver.kt                    # Reboot recovery
│       │       │   └── detector/                          # Modular Instagram & YouTube detectors
│       │       ├── ui/
│       │       │   ├── theme/           # Color, Shape, Type, Theme (Dark-first Material 3)
│       │       │   ├── navigation/      # Bottom Navigation, Routes, NavGraph
│       │       │   ├── components/      # ScoreGauge, UsageProgressBar, 7-Day Bar Chart, Cards
│       │       │   ├── home/            # HomeScreen & HomeViewModel
│       │       │   ├── limits/          # LimitsScreen & LimitsViewModel (Test Mode controls)
│       │       │   ├── analytics/       # AnalyticsScreen & AnalyticsViewModel
│       │       │   ├── settings/        # SettingsScreen & SettingsViewModel
│       │       │   ├── onboarding/      # 4-screen Onboarding flow
│       │       │   ├── permissions/     # Permission setup with real-time status & direct links
│       │       │   ├── intervention/    # Full-screen InterventionActivity & InterventionScreen
│       │       │   └── reflection/      # Mindful reflection dialog & flow
│       │       └── util/
│       │           ├── TimeUtils.kt     # Midnight reset, date math, formatters
│       │           └── DemoDataSeeder.kt # 7-day realistic demo seeder
│       └── test/java/com/example/scrollsense/
│           └── AttentionScoreTest.kt    # Unit tests for scoring logic
├── gradle/
│   ├── libs.versions.toml               # Version catalog (AGP 8.7.3, Kotlin 2.0.21, Compose BOM)
│   └── wrapper/
│       ├── gradle-wrapper.jar           # Official Gradle wrapper binary
│       └── gradle-wrapper.properties    # Gradle 8.11.1 distribution
├── build.gradle.kts                     # Root build file
├── settings.gradle.kts                  # Repository resolution & project inclusion
├── gradle.properties                    # JVM memory & AndroidX flags
├── gradlew                              # Unix/macOS Gradle executable wrapper script
├── gradlew.bat                          # Windows Gradle wrapper script
├── TECHNICAL_ARCHITECTURE.md            # Deep-dive on Android constraints & enforcement
└── README.md                            # Complete documentation
```
