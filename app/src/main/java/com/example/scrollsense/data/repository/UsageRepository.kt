package com.example.scrollsense.data.repository

import com.example.scrollsense.data.local.ScrollSenseDatabase
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.ReflectionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.data.preferences.UserPreferences
import com.example.scrollsense.data.preferences.UserPreferencesRepository
import com.example.scrollsense.domain.model.AttentionScore
import com.example.scrollsense.domain.model.DailyAppUsage
import com.example.scrollsense.domain.model.DailyInsight
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.domain.model.ReflectionMood
import com.example.scrollsense.domain.model.ReflectionTrigger
import com.example.scrollsense.domain.usecase.CalculateAttentionScoreUseCase
import com.example.scrollsense.domain.usecase.GenerateInsightsUseCase
import com.example.scrollsense.service.UsageTrackingManager
import com.example.scrollsense.util.DemoDataSeeder
import com.example.scrollsense.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class DailyHistoryItem(
    val dateMillis: Long,
    val dayLabel: String,
    val instagramMinutes: Int,
    val youtubeMinutes: Int,
    val totalMinutes: Int,
    val estimatedVideos: Int,
    val isOverLimit: Boolean
)

class UsageRepository(
    private val database: ScrollSenseDatabase,
    private val preferencesRepository: UserPreferencesRepository,
    private val usageTrackingManager: UsageTrackingManager
) {
    private val calculateScoreUseCase = CalculateAttentionScoreUseCase()
    private val generateInsightsUseCase = GenerateInsightsUseCase()

    val preferencesFlow: Flow<UserPreferences> = preferencesRepository.preferencesFlow

    fun getTodayAppUsage(): Flow<List<DailyAppUsage>> {
        return combine(
            preferencesRepository.preferencesFlow,
            database.usageRecordDao().getRecordsSince(TimeUtils.getStartOfDayForDate(0)),
            database.interventionDao().getInterventionsSince(TimeUtils.getStartOfDayForDate(0))
        ) { prefs, records, interventions ->
            val todayStart = TimeUtils.getStartOfDay(prefs.resetHour)
            ProtectedApp.entries.map { app ->
                val record = records.filter { it.packageName == app.packageName && it.dateEpochMillis >= todayStart }
                    .maxByOrNull { it.dateEpochMillis }
                    ?: records.firstOrNull { it.packageName == app.packageName }
                val appInterventions = interventions.count { it.packageName == app.packageName }

                val (limitMinutes, videoTarget) = when {
                    prefs.isTestModeEnabled -> Pair(prefs.testLimitMinutes, 10)
                    app == ProtectedApp.INSTAGRAM -> Pair(prefs.instagramLimitMinutes, prefs.instagramVideoTarget)
                    app == ProtectedApp.YOUTUBE -> Pair(prefs.youtubeLimitMinutes, prefs.youtubeVideoTarget)
                    else -> Pair(45, 50)
                }

                DailyAppUsage(
                    app = app,
                    usedMinutes = record?.foregroundMinutes ?: 0,
                    limitMinutes = limitMinutes,
                    estimatedVideos = record?.estimatedVideos ?: 0,
                    targetVideos = videoTarget,
                    interventionsCount = appInterventions
                )
            }
        }
    }

    fun getAttentionScore(): Flow<AttentionScore> {
        val sevenDaysAgo = TimeUtils.getStartOfDayForDate(6)
        return combine(
            preferencesRepository.preferencesFlow,
            database.usageRecordDao().getRecordsSince(sevenDaysAgo),
            database.interventionDao().getInterventionsSince(sevenDaysAgo)
        ) { prefs, records, interventions ->
            calculateScoreUseCase(
                usageRecords = records,
                interventions = interventions,
                instagramLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.instagramLimitMinutes,
                youtubeLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.youtubeLimitMinutes
            )
        }
    }

    fun getDailyInsight(): Flow<DailyInsight> {
        val startOfToday = TimeUtils.getStartOfDayForDate(0)
        val startOfYesterday = TimeUtils.getStartOfDayForDate(1)
        val endOfYesterday = TimeUtils.getEndOfDayForDate(1)

        return combine(
            preferencesRepository.preferencesFlow,
            database.usageRecordDao().getRecordsSince(startOfYesterday)
        ) { prefs, allRecords ->
            val todayRecords = allRecords.filter { it.dateEpochMillis >= startOfToday }
            val yesterdayRecords = allRecords.filter { it.dateEpochMillis in startOfYesterday..endOfYesterday }

            generateInsightsUseCase(
                todayRecords = todayRecords,
                yesterdayRecords = yesterdayRecords,
                instagramLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.instagramLimitMinutes,
                youtubeLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.youtubeLimitMinutes,
                isTestMode = prefs.isTestModeEnabled
            )
        }
    }

    fun getSevenDayHistory(): Flow<List<DailyHistoryItem>> {
        val sevenDaysAgo = TimeUtils.getStartOfDayForDate(6)
        return combine(
            preferencesRepository.preferencesFlow,
            database.usageRecordDao().getRecordsSince(sevenDaysAgo)
        ) { prefs, records ->
            val instaLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.instagramLimitMinutes
            val ytLimit = if (prefs.isTestModeEnabled) prefs.testLimitMinutes else prefs.youtubeLimitMinutes

            (6 downTo 0).map { daysAgo ->
                val dayStart = TimeUtils.getStartOfDayForDate(daysAgo)
                val dayEnd = TimeUtils.getEndOfDayForDate(daysAgo)
                val dayRecords = records.filter { it.dateEpochMillis in dayStart..dayEnd }

                val insta = dayRecords.firstOrNull { it.packageName == ProtectedApp.INSTAGRAM.packageName }
                val yt = dayRecords.firstOrNull { it.packageName == ProtectedApp.YOUTUBE.packageName }

                val instaMins = insta?.foregroundMinutes ?: 0
                val ytMins = yt?.foregroundMinutes ?: 0
                val totalMins = instaMins + ytMins
                val totalVideos = (insta?.estimatedVideos ?: 0) + (yt?.estimatedVideos ?: 0)
                val isOver = (instaMins > instaLimit && instaLimit > 0) || (ytMins > ytLimit && ytLimit > 0)

                DailyHistoryItem(
                    dateMillis = dayStart,
                    dayLabel = if (daysAgo == 0) "Today" else TimeUtils.formatDateShort(dayStart),
                    instagramMinutes = instaMins,
                    youtubeMinutes = ytMins,
                    totalMinutes = totalMins,
                    estimatedVideos = totalVideos,
                    isOverLimit = isOver
                )
            }
        }
    }

    suspend fun syncTodayUsageFromSystem() {
        val prefs = preferencesRepository.preferencesFlow.first()
        if (prefs.isDemoModeActive) return // Keep simulated data in demo mode

        if (!usageTrackingManager.hasUsageAccess()) return

        val startOfDay = TimeUtils.getStartOfDay(prefs.resetHour)
        val usageMap = usageTrackingManager.queryUsageForAllProtected(startOfDay)

        for ((app, usage) in usageMap) {
            val existing = database.usageRecordDao().getRecordSync(app.packageName, startOfDay)
            val currentEstimated = existing?.estimatedVideos ?: 0

            database.usageRecordDao().upsert(
                UsageRecordEntity(
                    id = existing?.id ?: 0L,
                    dateEpochMillis = startOfDay,
                    packageName = app.packageName,
                    foregroundMinutes = usage.foregroundMinutes,
                    estimatedVideos = currentEstimated,
                    lateNightMinutes = usage.lateNightMinutes,
                    longestSessionMinutes = usage.longestSessionMinutes
                )
            )
        }
    }

    suspend fun recordReflection(
        app: ProtectedApp,
        trigger: ReflectionTrigger,
        mood: ReflectionMood,
        notes: String
    ) {
        database.reflectionDao().insert(
            ReflectionEntity(
                timestamp = System.currentTimeMillis(),
                packageName = app.packageName,
                triggerReason = trigger.label,
                mood = mood.label,
                notes = notes
            )
        )
    }

    fun getRecentReflections(limit: Int = 20): Flow<List<ReflectionEntity>> {
        return database.reflectionDao().getRecentReflections(limit)
    }

    fun getInterventions(sinceMillis: Long = TimeUtils.getStartOfDayForDate(6)): Flow<List<InterventionEntity>> {
        return database.interventionDao().getInterventionsSince(sinceMillis)
    }

    suspend fun resetTodayUsage() {
        val startOfToday = TimeUtils.getStartOfDayForDate(0)
        database.usageRecordDao().deleteForDate(startOfToday)
        database.interventionDao().deleteSince(startOfToday)
    }

    suspend fun setDemoMode(active: Boolean) {
        preferencesRepository.setDemoModeActive(active)
        if (active) {
            DemoDataSeeder.seedDemoData(database)
        } else {
            DemoDataSeeder.clearDemoData(database)
            syncTodayUsageFromSystem()
        }
    }
}
