package com.example.scrollsense.domain.usecase

import com.example.scrollsense.data.local.entities.UsageRecordEntity
import com.example.scrollsense.domain.model.DailyInsight
import com.example.scrollsense.domain.model.InsightType
import com.example.scrollsense.domain.model.ProtectedApp
import kotlin.math.abs
import kotlin.math.roundToInt

class GenerateInsightsUseCase {

    operator fun invoke(
        todayRecords: List<UsageRecordEntity>,
        yesterdayRecords: List<UsageRecordEntity>,
        instagramLimit: Int,
        youtubeLimit: Int,
        isTestMode: Boolean
    ): DailyInsight {
        if (isTestMode) {
            return DailyInsight(
                title = "Test Mode Active",
                description = "Limits are currently scaled down to test intervention and blocking behavior without waiting all day.",
                type = InsightType.NEUTRAL
            )
        }

        val todayTotalMinutes = todayRecords.sumOf { it.foregroundMinutes }
        val yesterdayTotalMinutes = yesterdayRecords.sumOf { it.foregroundMinutes }

        val instaToday = todayRecords.firstOrNull { it.packageName == ProtectedApp.INSTAGRAM.packageName }?.foregroundMinutes ?: 0
        val ytToday = todayRecords.firstOrNull { it.packageName == ProtectedApp.YOUTUBE.packageName }?.foregroundMinutes ?: 0

        // Check if limit exceeded
        if (instaToday >= instagramLimit && instagramLimit > 0) {
            return DailyInsight(
                title = "Instagram Limit Reached",
                description = "You've reached your configured daily boundary for Instagram ($instaToday / $instagramLimit min). Step away and reset your focus.",
                type = InsightType.WARNING
            )
        }

        if (ytToday >= youtubeLimit && youtubeLimit > 0) {
            return DailyInsight(
                title = "YouTube Limit Reached",
                description = "You've reached your configured daily boundary for YouTube ($ytToday / $youtubeLimit min). Time to pause and recharge.",
                type = InsightType.WARNING
            )
        }

        // Check late night usage
        val todayLateNight = todayRecords.sumOf { it.lateNightMinutes }
        if (todayLateNight > 15) {
            return DailyInsight(
                title = "Late-Night Usage Alert",
                description = "$todayLateNight minutes were spent on protected apps during late-night hours. Protecting sleep hygiene improves tomorrow's focus.",
                type = InsightType.WARNING
            )
        }

        // Compare with yesterday if yesterday has data
        if (yesterdayTotalMinutes > 0 && todayTotalMinutes > 0) {
            val diff = todayTotalMinutes - yesterdayTotalMinutes
            val percentChange = ((abs(diff).toFloat() / yesterdayTotalMinutes) * 100).roundToInt()

            return if (diff < 0) {
                DailyInsight(
                    title = "Positive Momentum",
                    description = "You spent $percentChange% less time on protected apps today compared to yesterday. Your intentional boundary is working.",
                    type = InsightType.POSITIVE
                )
            } else {
                DailyInsight(
                    title = "Increased Consumption",
                    description = "You spent $percentChange% more time on protected apps today compared to yesterday. Keep an eye on your remaining allowances.",
                    type = InsightType.NEUTRAL
                )
            }
        }

        if (todayTotalMinutes > 0) {
            return DailyInsight(
                title = "Boundaries Respected",
                description = "All protected apps remain safely within your configured daily boundaries today. Intentional consumption builds sustained focus.",
                type = InsightType.POSITIVE
            )
        }

        return DailyInsight(
            title = "Fresh Beginning",
            description = "No protected app usage recorded yet today. Protect your morning attention for high-value priorities.",
            type = InsightType.POSITIVE
        )
    }
}
