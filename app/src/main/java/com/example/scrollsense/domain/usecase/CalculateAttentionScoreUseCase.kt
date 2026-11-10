package com.example.scrollsense.domain.usecase

import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.domain.model.AttentionScore
import kotlin.math.roundToInt

class CalculateAttentionScoreUseCase {

    operator fun invoke(
        usageRecords: List<UsageRecordEntity>,
        interventions: List<InterventionEntity>,
        instagramLimit: Int,
        youtubeLimit: Int
    ): AttentionScore {
        if (usageRecords.isEmpty()) {
            return AttentionScore.building()
        }

        var score = 100
        var totalLateNight = 0
        var totalOverLimitDays = 0
        val totalDays = (usageRecords.map { it.dateEpochMillis }.distinct().size).coerceAtLeast(1)

        // Group by date
        val byDate = usageRecords.groupBy { it.dateEpochMillis }

        for ((_, records) in byDate) {
            val instaMinutes = records.firstOrNull { it.packageName.contains("instagram") }?.foregroundMinutes ?: 0
            val ytMinutes = records.firstOrNull { it.packageName.contains("youtube") }?.foregroundMinutes ?: 0

            var dayOver = false
            if (instaMinutes > instagramLimit && instagramLimit > 0) {
                score -= 6
                dayOver = true
            }
            if (ytMinutes > youtubeLimit && youtubeLimit > 0) {
                score -= 6
                dayOver = true
            }
            if (dayOver) totalOverLimitDays++

            for (r in records) {
                totalLateNight += r.lateNightMinutes
                if (r.longestSessionMinutes > 40) {
                    score -= 4
                }
            }
        }

        // Penalty for late night usage
        val lateNightHours = totalLateNight / 60
        score -= (lateNightHours * 5).coerceAtMost(20)

        // Penalty for interventions triggered
        score -= (interventions.size * 3).coerceAtMost(15)

        val finalScore = score.coerceIn(12, 100)
        val adherencePercent = (((totalDays - totalOverLimitDays).toFloat() / totalDays) * 100).roundToInt().coerceIn(0, 100)

        val summary = when {
            finalScore >= 85 -> "Outstanding digital discipline. Your attention is focused and intentional."
            finalScore >= 70 -> "Solid attention resilience. Small improvements in late-night consumption will push you higher."
            finalScore >= 50 -> "Moderate consumption. You are frequently approaching or crossing daily limits."
            else -> "High consumption detected. Use ScrollSense interventions to reclaim calm attention."
        }

        return AttentionScore(
            score = finalScore,
            isBuilding = false,
            summary = summary,
            adherencePercent = adherencePercent,
            lateNightMinutes = totalLateNight,
            totalInterventions = interventions.size
        )
    }
}
