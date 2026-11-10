package com.example.scrollsense.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollsense.ScrollSenseApp
import com.example.scrollsense.data.preferences.UserPreferences
import com.example.scrollsense.domain.model.PermissionStatus
import com.example.scrollsense.domain.model.ThemeMode
import com.example.scrollsense.service.ScrollSenseAccessibilityService
import com.example.scrollsense.service.UsageTrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val permissions: PermissionStatus = PermissionStatus(false, false, false, false)
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ScrollSenseApp
    private val preferencesRepo = app.preferencesRepository
    private val usageRepo = app.usageRepository
    private val usageTracker = UsageTrackingManager(application)

    private val permissionState = MutableStateFlow(checkPermissions())

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepo.preferencesFlow,
        permissionState
    ) { prefs, perms ->
        SettingsUiState(preferences = prefs, permissions = perms)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun refreshPermissions() {
        permissionState.value = checkPermissions()
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

    fun setUserName(name: String) {
        viewModelScope.launch {
            preferencesRepo.setUserName(name)
        }
    }

    fun setWarningThreshold(percent: Int) {
        viewModelScope.launch {
            preferencesRepo.setWarningThreshold(percent)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setNotificationsEnabled(enabled)
        }
    }

    fun toggleTestMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setTestModeEnabled(enabled)
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            usageRepo.setDemoMode(enabled)
        }
    }

    fun resetTodayData() {
        viewModelScope.launch {
            usageRepo.resetTodayUsage()
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepo.setThemeMode(mode)
        }
    }
}
