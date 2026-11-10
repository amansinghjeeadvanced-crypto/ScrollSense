package com.example.scrollsense

import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.domain.usecase.CalculateAttentionScoreUseCase
import com.example.scrollsense.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttentionScoreTest {

    private val calculateScoreUseCase = CalculateAttentionScoreUseCase()

    @Test
    fun emptyRecords_returnsBuildingState() {
        val score = calculateScoreUseCase(
            usageRecords = emptyList(),
            interventions = emptyList(),
            instagramLimit = 30,
            youtubeLimit = 45
        )
        assertTrue(score.isBuilding)
        assertEquals(0, score.score)
    }

    @Test
    fun perfectAdherence_returnsHighAttentionScore() {
        val today = TimeUtils.getStartOfDay()
        val records = listOf(
            UsageRecordEntity(
                dateEpochMillis = today,
                packageName = ProtectedApp.INSTAGRAM.packageName,
                foregroundMinutes = 20,
                estimatedVideos = 25,
                lateNightMinutes = 0,
                longestSessionMinutes = 10
            ),
            UsageRecordEntity(
                dateEpochMillis = today,
                packageName = ProtectedApp.YOUTUBE.packageName,
                foregroundMinutes = 30,
                estimatedVideos = 35,
                lateNightMinutes = 0,
                longestSessionMinutes = 15
            )
        )

        val score = calculateScoreUseCase(
            usageRecords = records,
            interventions = emptyList(),
            instagramLimit = 30,
            youtubeLimit = 45
        )

        assertFalse(score.isBuilding)
        assertEquals(100, score.score)
        assertEquals(100, score.adherencePercent)
    }

    @Test
    fun exceededLimitsAndInterventions_penalizesScore() {
        val today = TimeUtils.getStartOfDay()
        val records = listOf(
            UsageRecordEntity(
                dateEpochMillis = today,
                packageName = ProtectedApp.INSTAGRAM.packageName,
                foregroundMinutes = 60, // Limit is 30
                estimatedVideos = 80,
                lateNightMinutes = 30,
                longestSessionMinutes = 45
            )
        )
        val interventions = listOf(
            InterventionEntity(
                timestamp = System.currentTimeMillis(),
                packageName = ProtectedApp.INSTAGRAM.packageName,
                usedMinutes = 60,
                limitMinutes = 30
            )
        )

        val score = calculateScoreUseCase(
            usageRecords = records,
            interventions = interventions,
            instagramLimit = 30,
            youtubeLimit = 45
        )

        assertFalse(score.isBuilding)
        assertTrue(score.score < 90)
    }
}
