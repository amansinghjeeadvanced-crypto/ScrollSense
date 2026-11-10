package com.example.scrollsense.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.util.TimeUtils

data class AppUsageStatsResult(
    val packageName: String,
    val foregroundTimeMillis: Long,
    val longestSessionMillis: Long,
    val lateNightMillis: Long
) {
    val foregroundMinutes: Int
        get() = (foregroundTimeMillis / (1000 * 60)).toInt()

    val longestSessionMinutes: Int
        get() = (longestSessionMillis / (1000 * 60)).toInt()

    val lateNightMinutes: Int
        get() = (lateNightMillis / (1000 * 60)).toInt()
}

class UsageTrackingManager(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun queryAppUsage(packageName: String, startTime: Long, endTime: Long = System.currentTimeMillis()): AppUsageStatsResult {
        if (!hasUsageAccess()) {
            return AppUsageStatsResult(packageName, 0L, 0L, 0L)
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return AppUsageStatsResult(packageName, 0L, 0L, 0L)

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var totalForegroundTime = 0L
        var longestSession = 0L
        var lateNightTime = 0L
        var sessionStartTime = 0L

        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.packageName == packageName) {
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        sessionStartTime = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (sessionStartTime > 0) {
                            val duration = event.timeStamp - sessionStartTime
                            if (duration > 0) {
                                totalForegroundTime += duration
                                if (duration > longestSession) {
                                    longestSession = duration
                                }
                                if (TimeUtils.isLateNight(sessionStartTime)) {
                                    lateNightTime += duration
                                }
                            }
                            sessionStartTime = 0L
                        }
                    }
                }
            }
        }

        // Handle case where app is currently active in the foreground right now
        if (sessionStartTime > 0) {
            val ongoingDuration = (endTime - sessionStartTime).coerceAtLeast(0L)
            totalForegroundTime += ongoingDuration
            if (ongoingDuration > longestSession) {
                longestSession = ongoingDuration
            }
            if (TimeUtils.isLateNight(sessionStartTime)) {
                lateNightTime += ongoingDuration
            }
        }

        // Secondary fallback to queryUsageStats if queryEvents had empty intervals on some OEM ROMs
        if (totalForegroundTime == 0L) {
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            val stat = statsList.firstOrNull { it.packageName == packageName }
            if (stat != null && stat.lastTimeUsed >= startTime) {
                totalForegroundTime = stat.totalTimeInForeground
                longestSession = (totalForegroundTime / 2).coerceAtLeast(0L)
            }
        }

        return AppUsageStatsResult(
            packageName = packageName,
            foregroundTimeMillis = totalForegroundTime,
            longestSessionMillis = longestSession,
            lateNightMillis = lateNightTime
        )
    }

    fun queryUsageForAllProtected(startTime: Long, endTime: Long = System.currentTimeMillis()): Map<ProtectedApp, AppUsageStatsResult> {
        return ProtectedApp.entries.associateWith { app ->
            queryAppUsage(app.packageName, startTime, endTime)
        }
    }
}
