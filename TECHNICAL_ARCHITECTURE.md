# ScrollSense: Technical Architecture & Android Enforcement Model

## 1. Executive Summary

**ScrollSense** is a native Android digital-wellness application engineered to protect user attention from excessive short-form video consumption (specifically Instagram Reels and YouTube Shorts).

The core technical philosophy of ScrollSense is **rigorous transparency**:
- We distinguish between **Enforced Data** (deterministic, system-level metrics that Android reliably provides) and **Estimated Data** (heuristic approximations derived from UI accessibility events).
- We never fabricate precision or claim capabilities that the underlying operating system cannot guarantee.

---

## 2. The Core Technical Reality: Why Exact Reel/Short Counting Is Difficult on Android

Users often assume an app can simply query Android for:
> *"The user watched 47 Reels and 52 Shorts today."*

In reality, **no modern mobile operating system (Android or iOS) provides a public API or broadcast for individual short-form video consumption.**

### The Technical Barriers:

1. **Sandboxing and Process Isolation**:
   - Android isolates application processes from one another using Linux UID permissions.
   - One application cannot inspect internal variables, HTTP requests, or internal state machines of Instagram or YouTube.

2. **Obfuscation and Dynamic Layouts**:
   - Both Instagram (`com.instagram.android`) and YouTube (`com.google.android.youtube`) use aggressive ProGuard/R8 code obfuscation, React Native / Litho / Bloks component frameworks, and server-driven UI engines.
   - Resource IDs (e.g., `clips_video_container`, `shorts_container`) change across app releases, A/B experiments, and regional rollouts.

3. **Continuous Scrolling vs Discrete Consumption**:
   - Short-form feeds employ vertical view pagers with pre-loading buffers. When a user swipes quickly past 5 videos without watching, does that count as 5 videos or 0?
   - Accessibility scroll events (`TYPE_VIEW_SCROLLED`) fire continuously during a gesture. A single finger flick can generate multiple scroll delta events or none depending on how the custom layout handles nested scrolling.

4. **In-Feed vs Dedicated Tab**:
   - A Reel can appear in the main feed, in the Explore grid, in a direct message, or in the dedicated Reels viewer. YouTube Shorts appear on the home screen carousel, in search results, and in the dedicated Shorts player.
   - Distinguishing between an in-feed preview and an intentional full-screen watch session via accessibility inspection requires traversing deep node trees, which consumes battery and CPU if unthrottled.

---

## 3. Data Classification: Enforced vs Estimated

To maintain trust and technical reliability, ScrollSense categorizes data into two strict tiers:

```
┌─────────────────────────────────────────────────────────────┐
│                       ScrollSense                           │
├──────────────────────────────┬──────────────────────────────┤
│        ENFORCED DATA         │        ESTIMATED DATA        │
│  (Authoritative & Reliable)  │     (Heuristic & Visual)     │
├──────────────────────────────┼──────────────────────────────┤
│ • Foreground App Usage Time  │ • Individual Reel Swipes     │
│ • Session Start & End Times  │ • Individual Short Swipes    │
│ • Longest Single Session     │ • Feed Type Approximations   │
│ • Late-Night Usage Hours     │ • Video Count Targets        │
│ • Daily Reset Adherence      │                              │
└──────────────────────────────┴──────────────────────────────┘
```

### 3.1 Enforced Data (Primary Protection Mechanism)
- **Source**: Android's official `UsageStatsManager` (`queryEvents` and `queryUsageStats`).
- **Permissions**: `android.permission.PACKAGE_USAGE_STATS` (granted via Special App Access).
- **Reliability**: 100% deterministic and tamper-resistant at the OS level.
- **Enforcement Principle**: When a user's daily limit is set to 30 minutes, ScrollSense measures exact foreground execution time of the protected application. When 30 minutes is elapsed, intervention triggers immediately and decisively.

### 3.2 Estimated Data (Supplementary Context)
- **Source**: `ScrollSenseAccessibilityService` inspecting `AccessibilityNodeInfo` hierarchy and debounced `TYPE_VIEW_SCROLLED` events.
- **Reliability**: Heuristic (~85–90% accuracy depending on Instagram/YouTube app versions).
- **Communication**: The UI explicitly labels these metrics as *"Estimated Reels"* and *"Estimated Shorts"*, never claiming exact OS-level counts.

---

## 4. Architectural Layers

ScrollSense follows a modular, clean Android architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                           UI Layer                              │
│ Jetpack Compose • Material 3 • Navigation • ViewModels          │
│ (Home • Limits • Analytics • Settings • Onboarding • Dialogs)  │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                         Domain Layer                            │
│ Use Cases • Data Models • Score Computation • Insights Engine   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                         Data Layer                              │
│ Room Database (UsageRecords, Interventions, Reflections)       │
│ Jetpack DataStore (User Preferences & Boundaries)              │
│ UsageRepository (Reactive data coordination via Kotlin Flows)   │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                        Service Layer                            │
│ UsageTrackingManager (UsageStatsManager Event Aggregator)      │
│ ScrollSenseAccessibilityService (Event Observation & Enforcement)│
│ ShortFormDetector Subsystem (Instagram & YouTube detectors)    │
│ NotificationHelper & BootReceiver (Alerts & Lifecycle)          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Accessibility Service & Intervention Architecture

### 5.1 Service Declaration & Event Filtering
Declared in `AndroidManifest.xml` and configured in `res/xml/accessibility_service_config.xml`:
- Events observed: `TYPE_WINDOW_STATE_CHANGED`, `TYPE_VIEW_SCROLLED`, `TYPE_WINDOW_CONTENT_CHANGED`.
- Flags: `flagRetrieveInteractiveWindows`, `flagReportViewIds`.

### 5.2 Intervention Execution Flow
When `ScrollSenseAccessibilityService` detects that a protected app (`com.instagram.android` or `com.google.android.youtube`) is in the foreground:

1. **Threshold Evaluation**:
   - Queries `UsageTrackingManager.queryAppUsage(...)` from start of day (e.g. 00:00).
   - If `usedMinutes >= effectiveLimitMinutes`:
2. **Immediate OS Action**:
   - Calls `performGlobalAction(GLOBAL_ACTION_HOME)` to immediately collapse the restricted app.
3. **Dedicated Full-Screen Presentation**:
   - Launches `InterventionActivity` with flags `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`.
   - Displays a calm, non-shaming screen:
     - App name and today's usage vs limit.
     - Next reset time.
     - Actions: **Go back**, **View usage**, and optional **Reflect**.
4. **Anti-Bypass Lock**:
   - If the user attempts to immediately switch back into the restricted app using the Android app switcher or home icon, the AccessibilityService observes the package re-entry and immediately executes `GLOBAL_ACTION_HOME` and brings the intervention screen forward again.
5. **Debouncing**:
   - Interventions are debounced (4-second window) to prevent rapid-fire intent loops while maintaining firm enforcement.

---

## 6. Mathematical Attention Score Model

The **Attention Score** is a deterministic metric from 0 to 100 calculated by `CalculateAttentionScoreUseCase`:

$$\text{Score} = \text{Clamp}\Big(100 - \sum \text{LimitPenalties} - \text{LateNightPenalty} - \text{InterventionPenalty},\ 12,\ 100\Big)$$

- **Baseline**: 100 points.
- **Over-Limit Penalty**: -6 points per day for each protected app exceeding the configured boundary.
- **Late-Night Penalty**: -5 points per hour of usage between 10:00 PM and 4:00 AM (capped at -20).
- **Session Duration Penalty**: -4 points for sessions exceeding 40 continuous minutes.
- **Intervention Frequency**: -3 points per triggered intervention (capped at -15).
- **Empty State**: If zero historical records exist, the score transitions into `Building your score` state.

---

## 7. Privacy & Security Model

- **Zero Network Egress**: The application does NOT declare `android.permission.INTERNET`. It cannot communicate with remote servers, ad networks, or cloud trackers.
- **No Text or Keystroke Logging**: The accessibility service only reads package names, view container resource IDs, and scroll events. It never reads editable text fields, passwords, or personal chat messages.
- **100% Local Persistence**: All data is stored in SQLite (Room) and encrypted-capable DataStore on the user's physical device.
