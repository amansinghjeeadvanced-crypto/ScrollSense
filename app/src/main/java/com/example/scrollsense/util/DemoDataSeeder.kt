package com.example.scrollsense.util

import com.example.scrollsense.data.local.ScrollSenseDatabase
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.ReflectionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.domain.model.ReflectionMood
import com.example.scrollsense.domain.model.ReflectionTrigger

object DemoDataSeeder {

    suspend fun seedDemoData(database: ScrollSenseDatabase) {
        // Clear previous entries
        database.usageRecordDao().clearAll()
        database.interventionDao().clearAll()
        database.reflectionDao().clearAll()

        val usageRecords = mutableListOf<UsageRecordEntity>()
        val interventions = mutableListOf<InterventionEntity>()
        val reflections = mutableListOf<ReflectionEntity>()

        // 7-day usage pattern
        // Days ago: 6, 5, 4, 3, 2, 1, 0 (0 is today)
        val sampleInstagramMinutes = listOf(28, 42, 35, 20, 48, 25, 32)
        val sampleInstagramVideos = listOf(38, 56, 47, 26, 64, 34, 43)

        val sampleYouTubeMinutes = listOf(35, 55, 40, 30, 62, 38, 41)
        val sampleYouTubeVideos = listOf(42, 65, 50, 36, 75, 46, 50)

        for (i in 0..6) {
            val daysAgo = 6 - i
            val dateMillis = TimeUtils.getStartOfDayForDate(daysAgo)

            // Instagram record
            usageRecords.add(
                UsageRecordEntity(
                    dateEpochMillis = dateMillis,
                    packageName = ProtectedApp.INSTAGRAM.packageName,
                    foregroundMinutes = sampleInstagramMinutes[i],
                    estimatedVideos = sampleInstagramVideos[i],
                    lateNightMinutes = if (i % 3 == 0) 12 else 0,
                    longestSessionMinutes = 18
                )
            )

            // YouTube record
            usageRecords.add(
                UsageRecordEntity(
                    dateEpochMillis = dateMillis,
                    packageName = ProtectedApp.YOUTUBE.packageName,
                    foregroundMinutes = sampleYouTubeMinutes[i],
                    estimatedVideos = sampleYouTubeVideos[i],
                    lateNightMinutes = if (i % 2 == 0) 15 else 0,
                    longestSessionMinutes = 24
                )
            )
        }

        database.usageRecordDao().insertAll(usageRecords)

        // Seed sample interventions
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        interventions.add(
            InterventionEntity(
                timestamp = now - (2 * oneDayMs) + (14 * 3600 * 1000),
                packageName = ProtectedApp.INSTAGRAM.packageName,
                usedMinutes = 35,
                limitMinutes = 30
            )
        )
        interventions.add(
            InterventionEntity(
                timestamp = now - (4 * oneDayMs) + (21 * 3600 * 1000),
                packageName = ProtectedApp.YOUTUBE.packageName,
                usedMinutes = 52,
                limitMinutes = 45
            )
        )
        interventions.add(
            InterventionEntity(
                timestamp = now - (2 * 3600 * 1000),
                packageName = ProtectedApp.INSTAGRAM.packageName,
                usedMinutes = 32,
                limitMinutes = 30
            )
        )

        database.interventionDao().insertAll(interventions)

        // Seed sample reflections
        reflections.add(
            ReflectionEntity(
                timestamp = now - (2 * 3600 * 1000),
                packageName = ProtectedApp.INSTAGRAM.packageName,
                triggerReason = ReflectionTrigger.BOREDOM.label,
                mood = ReflectionMood.RESTLESS.label,
                notes = "Opened while waiting for meeting to start"
            )
        )
        reflections.add(
            ReflectionEntity(
                timestamp = now - (1 * oneDayMs) + (22 * 3600 * 1000),
                packageName = ProtectedApp.YOUTUBE.packageName,
                triggerReason = ReflectionTrigger.STRESS.label,
                mood = ReflectionMood.TIRED.label,
                notes = "Late night unwind winding up too long"
            )
        )
        reflections.add(
            ReflectionEntity(
                timestamp = now - (3 * oneDayMs) + (16 * 3600 * 1000),
                packageName = ProtectedApp.INSTAGRAM.packageName,
                triggerReason = ReflectionTrigger.HABIT.label,
                mood = ReflectionMood.CALM.label,
                notes = "Subconscious phone unlock"
            )
        )
        reflections.add(
            ReflectionEntity(
                timestamp = now - (5 * oneDayMs) + (11 * 3600 * 1000),
                packageName = ProtectedApp.YOUTUBE.packageName,
                triggerReason = ReflectionTrigger.AVOIDING_WORK.label,
                mood = ReflectionMood.ANXIOUS.label,
                notes = "Procrastinating on coding task"
            )
        )

        database.reflectionDao().insertAll(reflections)
    }

    suspend fun clearDemoData(database: ScrollSenseDatabase) {
        database.usageRecordDao().clearAll()
        database.interventionDao().clearAll()
        database.reflectionDao().clearAll()
    }
}
