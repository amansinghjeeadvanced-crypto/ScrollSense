package com.example.scrollsense.domain.model

data class AppLimit(
    val app: ProtectedApp,
    val limitMinutes: Int,
    val videoTarget: Int = 50,
    val isEnabled: Boolean = true
)

enum class LimitStatus {
    SAFE,
    NEAR_LIMIT,
    LIMIT_REACHED
}

data class DailyAppUsage(
    val app: ProtectedApp,
    val usedMinutes: Int,
    val limitMinutes: Int,
    val estimatedVideos: Int = 0,
    val targetVideos: Int = 50,
    val interventionsCount: Int = 0
) {
    val progress: Float
        get() = if (limitMinutes > 0) (usedMinutes.toFloat() / limitMinutes).coerceIn(0f, 1f) else 0f

    val remainingMinutes: Int
        get() = (limitMinutes - usedMinutes).coerceAtLeast(0)

    val status: LimitStatus
        get() = when {
            usedMinutes >= limitMinutes -> LimitStatus.LIMIT_REACHED
            progress >= 0.8f -> LimitStatus.NEAR_LIMIT
            else -> LimitStatus.SAFE
        }
}
