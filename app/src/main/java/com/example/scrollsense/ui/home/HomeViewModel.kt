package com.example.scrollsense.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollsense.ScrollSenseApp
import com.example.scrollsense.domain.model.AttentionScore
import com.example.scrollsense.domain.model.DailyAppUsage
import com.example.scrollsense.domain.model.DailyInsight
import com.example.scrollsense.domain.model.PermissionStatus
import com.example.scrollsense.service.ScrollSenseAccessibilityService
import com.example.scrollsense.service.UsageTrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val userName: String = "Aman",
    val greeting: String = "Good morning",
    val attentionScore: AttentionScore = AttentionScore.building(),
    val appUsageList: List<DailyAppUsage> = emptyList(),
    val unifiedShortVideosCount: Int = 0,
    val unifiedShortVideosLimit: Int = 50,
    val dailyInsight: DailyInsight = DailyInsight("Welcome to ScrollSense", "Setting intentional boundaries protects your daily attention."),
    val permissionStatus: PermissionStatus = PermissionStatus(false, false, false, false),
    val isTestMode: Boolean = false,
    val isDemoMode: Boolean = false,
    val isRefreshing: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ScrollSenseApp
    private val usageRepo = app.usageRepository
    private val usageTracker = UsageTrackingManager(application)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val permissionState = MutableStateFlow(checkPermissions())

    val uiState: StateFlow<HomeUiState> = combine(
        usageRepo.preferencesFlow,
        usageRepo.getAttentionScore(),
        usageRepo.getTodayAppUsage(),
        usageRepo.getDailyInsight(),
        permissionState
    ) { prefs, score, usages, insight, perms ->
        HomeUiState(
            userName = prefs.userName,
            greeting = getGreetingForTime(),
            attentionScore = score,
            appUsageList = usages,
            unifiedShortVideosCount = prefs.unifiedShortVideosCount,
            unifiedShortVideosLimit = if (prefs.isTestModeEnabled) 3 else prefs.unifiedShortVideosLimit,
            dailyInsight = insight,
            permissionStatus = perms,
            isTestMode = prefs.isTestModeEnabled,
            isDemoMode = prefs.isDemoModeActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        refreshUsage()
    }

    fun refreshUsage() {
        viewModelScope.launch {
            _isRefreshing.value = true
            permissionState.value = checkPermissions()
            usageRepo.syncTodayUsageFromSystem()
            _isRefreshing.value = false
        }
    }

    private fun checkPermissions(): PermissionStatus {
        val context = getApplication<Application>()
        return PermissionStatus(
            usageAccessGranted = usageTracker.hasUsageAccess(),
            accessibilityGranted = ScrollSenseAccessibilityService.isAccessibilityEnabled(context),
            notificationsGranted = true,
            overlayGranted = true
        )
    }

    private fun getGreetingForTime(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Late night"
        }
    }
}
