package com.example.scrollsense.domain.model

data class AttentionScore(
    val score: Int, // 0 to 100
    val isBuilding: Boolean = false,
    val summary: String,
    val adherencePercent: Int = 100,
    val lateNightMinutes: Int = 0,
    val totalInterventions: Int = 0
) {
    companion object {
        fun building(): AttentionScore = AttentionScore(
            score = 0,
            isBuilding = true,
            summary = "Building your score. ScrollSense needs at least 1 day of usage data to calculate attention resilience."
        )
    }
}

enum class InsightType {
    POSITIVE,
    WARNING,
    NEUTRAL
}

data class DailyInsight(
    val title: String,
    val description: String,
    val type: InsightType = InsightType.NEUTRAL
)

enum class ReflectionTrigger(val label: String) {
    BOREDOM("Boredom"),
    STRESS("Stress"),
    HABIT("Habit"),
    ENTERTAINMENT("Entertainment"),
    AVOIDING_WORK("Avoiding work"),
    SOCIAL_CONNECTION("Social connection"),
    JUST_OPENED("I just opened it"),
    OTHER("Other")
}

enum class ReflectionMood(val label: String, val emoji: String) {
    CALM("Calm", "😌"),
    FOCUSED("Focused", "🎯"),
    TIRED("Tired", "🥱"),
    ANXIOUS("Anxious", "😟"),
    RESTLESS("Restless", "🌀")
}

data class Reflection(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val app: ProtectedApp,
    val trigger: ReflectionTrigger,
    val mood: ReflectionMood,
    val note: String = ""
)

data class InterventionRecord(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val app: ProtectedApp,
    val usedMinutes: Int,
    val limitMinutes: Int
)

data class PermissionStatus(
    val usageAccessGranted: Boolean,
    val accessibilityGranted: Boolean,
    val notificationsGranted: Boolean,
    val overlayGranted: Boolean
) {
    val isProtectionActive: Boolean
        get() = usageAccessGranted && accessibilityGranted
}

enum class ThemeMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System Default")
}
