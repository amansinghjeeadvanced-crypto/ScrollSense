package com.example.scrollsense.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollsense.ScrollSenseApp
import com.example.scrollsense.data.local.entities.InterventionEntity
import com.example.scrollsense.data.local.entities.ReflectionEntity
import com.example.scrollsense.data.repository.DailyHistoryItem
import com.example.scrollsense.domain.model.AttentionScore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalyticsUiState(
    val attentionScore: AttentionScore = AttentionScore.building(),
    val sevenDayHistory: List<DailyHistoryItem> = emptyList(),
    val interventions: List<InterventionEntity> = emptyList(),
    val reflections: List<ReflectionEntity> = emptyList(),
    val isDemoMode: Boolean = false
) {
    val totalProtectedMinutes: Int
        get() = sevenDayHistory.sumOf { it.totalMinutes }

    val averageDailyMinutes: Int
        get() = if (sevenDayHistory.isNotEmpty()) totalProtectedMinutes / sevenDayHistory.size else 0

    val triggerBreakdown: Map<String, Int>
        get() = reflections.groupBy { it.triggerReason }.mapValues { it.value.size }

    val moodBreakdown: Map<String, Int>
        get() = reflections.groupBy { it.mood }.mapValues { it.value.size }

    val hasSufficientData: Boolean
        get() = sevenDayHistory.any { it.totalMinutes > 0 } || reflections.isNotEmpty()
}

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ScrollSenseApp
    private val usageRepo = app.usageRepository

    val uiState: StateFlow<AnalyticsUiState> = combine(
        usageRepo.getAttentionScore(),
        usageRepo.getSevenDayHistory(),
        usageRepo.getInterventions(),
        usageRepo.getRecentReflections(),
        usageRepo.preferencesFlow
    ) { score, history, interventions, reflections, prefs ->
        AnalyticsUiState(
            attentionScore = score,
            sevenDayHistory = history,
            interventions = interventions,
            reflections = reflections,
            isDemoMode = prefs.isDemoModeActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
}
