package com.example.scrollsense.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.scrollsense.data.local.ScrollSenseDatabase
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.data.preferences.UserPreferencesRepository
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.service.detector.InstagramDetector
import com.example.scrollsense.service.detector.ShortFormDetector
import com.example.scrollsense.service.detector.YouTubeDetector
import com.example.scrollsense.service.overlay.CountOverlayManager
import com.example.scrollsense.ui.intervention.InterventionActivity
import com.example.scrollsense.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class ScrollSenseAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var preferencesRepo: UserPreferencesRepository
    private lateinit var database: ScrollSenseDatabase
    private lateinit var usageTracker: UsageTrackingManager
    private lateinit var overlayManager: CountOverlayManager

    private val detectors = mutableMapOf<String, ShortFormDetector>()

    // Authoritative in-memory atomic unified daily short-form video counter
    private val unifiedDailyCount = AtomicInteger(0)
    private var cachedStartOfDayEpoch: Long = 0L

    // Runtime state tracking for transition detection and deduplication
    private var currentFeedType: String = "NONE"
    private var currentVisibleItemFingerprint: String? = null
    private var lastCountedItemFingerprint: String? = null
    private var lastTransitionTimestamp: Long = 0L

    private var lastInterventionTimestamp: Long = 0L
    private val interventionDebounceMs: Long = 4000L

    companion object {
        private const val TAG = "ScrollSense_Detection"

        @Volatile
        var isServiceRunning: Boolean = false
            private set

        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${ScrollSenseAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(expectedServiceName) ||
                    enabledServices.contains(context.packageName)
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferencesRepo = UserPreferencesRepository(applicationContext)
        database = ScrollSenseDatabase.getInstance(applicationContext)
        usageTracker = UsageTrackingManager(applicationContext)
        overlayManager = CountOverlayManager(this)

        detectors["com.instagram.android"] = InstagramDetector()
        detectors["com.google.android.youtube"] = YouTubeDetector()

        // Load authoritative unified daily counter from persistent storage
        serviceScope.launch {
            val prefs = preferencesRepo.preferencesFlow.first()
            val startOfDay = TimeUtils.getStartOfDay(prefs.resetHour)
            cachedStartOfDayEpoch = startOfDay
            val savedCount = preferencesRepo.getUnifiedDailyShortsCount(startOfDay)
            unifiedDailyCount.set(savedCount)
            Log.i(TAG, "ScrollSense initialized. Authoritative unified count for today: $savedCount")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SELECTED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 20 // Fast event dispatch
        serviceInfo = info

        Log.i(TAG, "ScrollSenseAccessibilityService connected with unified short-form counter.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        val protectedApp = ProtectedApp.fromPackageName(pkg)

        if (protectedApp == null) {
            // User navigated outside protected apps
            if (currentFeedType != "NONE") {
                currentFeedType = "NONE"
                currentVisibleItemFingerprint = null
                lastCountedItemFingerprint = null
                detectors.values.forEach { it.onAppLeft() }
                overlayManager.hide()
            }
            return
        }

        // Retrieve active root node synchronously on main thread
        val rootNode = rootInActiveWindow ?: event.source
        val detector = detectors[pkg] ?: return

        // Check if inside short-form feed
        val isShortForm = detector.isShortFormVisible(rootNode)
        if (!isShortForm) {
            // User is in YouTube or Instagram, but NOT on short-form feed
            if (currentFeedType != "NONE") {
                currentFeedType = "NONE"
                currentVisibleItemFingerprint = null
                lastCountedItemFingerprint = null
                detector.onAppLeft()
                overlayManager.hide()
            }

            // Monitor background foreground time limits
            serviceScope.launch {
                checkAndEnforceTimeLimit(protectedApp)
            }
            return
        }

        val feedType = if (protectedApp == ProtectedApp.YOUTUBE) "YOUTUBE_SHORTS" else "INSTAGRAM_REELS"
        currentFeedType = feedType

        // Extract visible item fingerprint
        val currentFingerprint = detector.extractItemFingerprint(rootNode)
        currentVisibleItemFingerprint = currentFingerprint

        val now = System.currentTimeMillis()

        // Verify daily reset point
        val startOfDay = TimeUtils.getStartOfDay(0)
        if (startOfDay != cachedStartOfDayEpoch) {
            cachedStartOfDayEpoch = startOfDay
            serviceScope.launch {
                val countToday = preferencesRepo.getUnifiedDailyShortsCount(startOfDay)
                unifiedDailyCount.set(countToday)
            }
            lastCountedItemFingerprint = null
        }

        val countBefore = unifiedDailyCount.get()
        var isNewTransition = false
        var countAfter = countBefore

        if (countBefore == 0) {
            // First detected Short/Reel of the day -> becomes 1!
            isNewTransition = true
            countAfter = unifiedDailyCount.incrementAndGet()
            lastCountedItemFingerprint = currentFingerprint.ifEmpty { "initial_item" }
            lastTransitionTimestamp = now
            overlayManager.updateCount(countAfter)
            persistCountIncrement(protectedApp, startOfDay, countAfter)
        } else if (lastCountedItemFingerprint == null) {
            // User opened feed with an existing count from earlier today (e.g. 47)
            // Show existing count immediately and lock initial item so entering does NOT increment
            lastCountedItemFingerprint = currentFingerprint.ifEmpty { "feed_entered" }
            overlayManager.updateCount(countBefore)
            isNewTransition = false
        } else {
            // Currently in feed, evaluate transition to NEW short-form video
            if (currentFingerprint.isNotBlank()) {
                if (currentFingerprint != lastCountedItemFingerprint) {
                    // Item fingerprint changed -> New video has appeared!
                    isNewTransition = true
                    countAfter = unifiedDailyCount.incrementAndGet()
                    lastCountedItemFingerprint = currentFingerprint
                    lastTransitionTimestamp = now
                    overlayManager.updateCount(countAfter)
                    persistCountIncrement(protectedApp, startOfDay, countAfter)
                } else {
                    // Fingerprint is identical -> Same video playing or looping (deduplicated)
                    isNewTransition = false
                }
            } else {
                // Fallback for uncaptioned video or ad via vertical scroll gesture
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
                    event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED
                ) {
                    if (now - lastTransitionTimestamp > 450L) {
                        isNewTransition = true
                        countAfter = unifiedDailyCount.incrementAndGet()
                        lastTransitionTimestamp = now
                        overlayManager.updateCount(countAfter)
                        persistCountIncrement(protectedApp, startOfDay, countAfter)
                    }
                }
            }
        }

        // Structured diagnostic logging
        logDiagnostic(
            pkg = pkg,
            event = event,
            rootNode = rootNode,
            feedState = feedType,
            fingerprint = currentFingerprint.ifEmpty { "none" },
            transitionDetected = isNewTransition,
            counterBefore = countBefore,
            counterAfter = countAfter
        )

        // Evaluate limits
        serviceScope.launch {
            checkAndEnforceLimits(protectedApp, countAfter, startOfDay)
        }
    }

    private fun persistCountIncrement(app: ProtectedApp, startOfDay: Long, newCount: Int) {
        serviceScope.launch {
            // Persist authoritative unified daily counter in DataStore
            preferencesRepo.setUnifiedDailyShortsCount(startOfDay, newCount)

            // Also record in Room database for local app history
            val usageResult = usageTracker.queryAppUsage(app.packageName, startOfDay)
            val updated = database.usageRecordDao().incrementEstimatedVideos(app.packageName, startOfDay)
            if (updated == 0) {
                database.usageRecordDao().upsert(
                    UsageRecordEntity(
                        dateEpochMillis = startOfDay,
                        packageName = app.packageName,
                        foregroundMinutes = usageResult.foregroundMinutes,
                        estimatedVideos = 1,
                        lateNightMinutes = usageResult.lateNightMinutes,
                        longestSessionMinutes = usageResult.longestSessionMinutes
                    )
                )
            }
            Log.d(TAG, "Unified daily count saved: $newCount (App: ${app.displayName})")
        }
    }

    private suspend fun checkAndEnforceTimeLimit(app: ProtectedApp) {
        val prefs = preferencesRepo.preferencesFlow.first()
        if (prefs.isDemoModeActive) return

        val isEnabled = when (app) {
            ProtectedApp.INSTAGRAM -> prefs.instagramEnabled
            ProtectedApp.YOUTUBE -> prefs.youtubeEnabled
        }
        if (!isEnabled) return

        val effectiveTimeLimit = when {
            prefs.isTestModeEnabled -> prefs.testLimitMinutes
            app == ProtectedApp.INSTAGRAM -> prefs.instagramLimitMinutes
            app == ProtectedApp.YOUTUBE -> prefs.youtubeLimitMinutes
            else -> 60
        }

        val startOfDay = TimeUtils.getStartOfDay(prefs.resetHour)
        val usageResult = usageTracker.queryAppUsage(app.packageName, startOfDay)
        val usedMinutes = usageResult.foregroundMinutes

        if (usedMinutes >= effectiveTimeLimit && effectiveTimeLimit > 0) {
            triggerIntervention(
                app = app,
                usedMinutes = usedMinutes,
                limitMinutes = effectiveTimeLimit,
                usedVideos = unifiedDailyCount.get(),
                limitVideos = if (prefs.isTestModeEnabled) 3 else prefs.unifiedShortVideosLimit,
                isVideoLimit = false
            )
        }
    }

    private suspend fun checkAndEnforceLimits(app: ProtectedApp, currentVideoCount: Int, startOfDay: Long) {
        val prefs = preferencesRepo.preferencesFlow.first()
        if (prefs.isDemoModeActive) return

        val effectiveTimeLimit = when {
            prefs.isTestModeEnabled -> prefs.testLimitMinutes
            app == ProtectedApp.INSTAGRAM -> prefs.instagramLimitMinutes
            app == ProtectedApp.YOUTUBE -> prefs.youtubeLimitMinutes
            else -> 60
        }

        val effectiveVideoLimit = when {
            prefs.isTestModeEnabled -> 3 // Quick test with 3 videos in test mode
            else -> prefs.unifiedShortVideosLimit
        }

        val usageResult = usageTracker.queryAppUsage(app.packageName, startOfDay)
        val usedMinutes = usageResult.foregroundMinutes

        val isTimeLimitReached = usedMinutes >= effectiveTimeLimit && effectiveTimeLimit > 0
        val isVideoLimitReached = currentVideoCount >= effectiveVideoLimit && effectiveVideoLimit > 0

        if (isTimeLimitReached || isVideoLimitReached) {
            triggerIntervention(
                app = app,
                usedMinutes = usedMinutes,
                limitMinutes = effectiveTimeLimit,
                usedVideos = currentVideoCount,
                limitVideos = effectiveVideoLimit,
                isVideoLimit = isVideoLimitReached
            )
        }
    }

    private fun triggerIntervention(
        app: ProtectedApp,
        usedMinutes: Int,
        limitMinutes: Int,
        usedVideos: Int,
        limitVideos: Int,
        isVideoLimit: Boolean
    ) {
        val now = System.currentTimeMillis()
        if (now - lastInterventionTimestamp < interventionDebounceMs) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            overlayManager.hide()
            return
        }
        lastInterventionTimestamp = now

        Log.w(
            TAG,
            "UNIFIED INTERVENTION TRIGGERED! Reason: ${if (isVideoLimit) "Unified short videos limit reached ($usedVideos / $limitVideos)" else "Time limit reached ($usedMinutes / $limitMinutes min)"}"
        )

        // Hide overlay immediately
        overlayManager.hide()

        // Kick out to HOME
        performGlobalAction(GLOBAL_ACTION_HOME)

        // Launch intervention screen
        val intent = Intent(applicationContext, InterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, app.packageName)
            putExtra(InterventionActivity.EXTRA_USED_MINUTES, usedMinutes)
            putExtra(InterventionActivity.EXTRA_LIMIT_MINUTES, limitMinutes)
            putExtra(InterventionActivity.EXTRA_USED_VIDEOS, usedVideos)
            putExtra(InterventionActivity.EXTRA_LIMIT_VIDEOS, limitVideos)
            putExtra(InterventionActivity.EXTRA_IS_VIDEO_LIMIT, isVideoLimit)
        }
        startActivity(intent)

        serviceScope.launch {
            database.interventionDao().insert(
                InterventionEntity(
                    timestamp = now,
                    packageName = app.packageName,
                    usedMinutes = usedMinutes,
                    limitMinutes = limitMinutes
                )
            )
        }
    }

    private fun logDiagnostic(
        pkg: String,
        event: AccessibilityEvent,
        rootNode: AccessibilityNodeInfo?,
        feedState: String,
        fingerprint: String,
        transitionDetected: Boolean,
        counterBefore: Int,
        counterAfter: Int
    ) {
        val (textSample, descSample) = extractNodeTextSamples(rootNode)
        val sourceNodeClass = event.source?.className?.toString() ?: rootNode?.className?.toString() ?: "unknown"
        val resourceId = event.source?.viewIdResourceName ?: "none"

        Log.d(
            TAG,
            """
            |--- [SCROLLSENSE UNIFIED DIAGNOSTIC] ---
            |[PACKAGE]: $pkg
            |[EVENT_TYPE]: ${AccessibilityEvent.eventTypeToString(event.eventType)}
            |[TIMESTAMP]: ${event.eventTime} (wall: ${System.currentTimeMillis()})
            |[SOURCE_NODE]: $sourceNodeClass
            |[RESOURCE_ID]: $resourceId
            |[VISIBLE_TEXT/DESC]: text="$textSample" | desc="$descSample"
            |[FEED_STATE]: $feedState
            |[ITEM_FINGERPRINT]: $fingerprint
            |[TRANSITION_DETECTED]: $transitionDetected
            |[COUNTER]: before=$counterBefore -> after=$counterAfter
            |---------------------------------------
            """.trimMargin()
        )
    }

    private fun extractNodeTextSamples(rootNode: AccessibilityNodeInfo?): Pair<String, String> {
        if (rootNode == null) return Pair("none", "none")
        val texts = mutableListOf<String>()
        val descs = mutableListOf<String>()

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        var count = 0

        while (queue.isNotEmpty() && count < 60) {
            val node = queue.removeFirst()
            count++

            val t = node.text?.toString()?.trim()
            if (!t.isNullOrEmpty() && t.length > 2 && texts.size < 2) {
                texts.add(t.take(30))
            }

            val d = node.contentDescription?.toString()?.trim()
            if (!d.isNullOrEmpty() && d.length > 2 && descs.size < 2) {
                descs.add(d.take(30))
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                queue.add(child)
            }
        }

        return Pair(
            if (texts.isEmpty()) "none" else texts.joinToString(" | "),
            if (descs.isEmpty()) "none" else descs.joinToString(" | ")
        )
    }

    override fun onInterrupt() {
        Log.w(TAG, "ScrollSenseAccessibilityService interrupted.")
        overlayManager.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        detectors.values.forEach { it.onAppLeft() }
        overlayManager.destroy()
        serviceScope.cancel()
        Log.i(TAG, "ScrollSenseAccessibilityService destroyed.")
    }
}
