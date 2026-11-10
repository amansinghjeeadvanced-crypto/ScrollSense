package com.example.scrollsense.ui.limits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrollsense.ScrollSenseApp
import com.example.scrollsense.domain.model.ProtectedApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LimitsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ScrollSenseApp
    private val preferencesRepo = app.preferencesRepository
    private val usageRepo = app.usageRepository

    val preferences = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.scrollsense.data.preferences.UserPreferences()
    )

    fun updateUnifiedShortVideosLimit(limit: Int) {
        viewModelScope.launch {
            preferencesRepo.setUnifiedShortVideosLimit(limit)
        }
    }

    fun updateInstagramLimit(minutes: Int, videoTarget: Int, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setAppLimit(ProtectedApp.INSTAGRAM, minutes, videoTarget, enabled)
        }
    }

    fun updateYouTubeLimit(minutes: Int, videoTarget: Int, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setAppLimit(ProtectedApp.YOUTUBE, minutes, videoTarget, enabled)
        }
    }

    fun updateResetHour(hour: Int) {
        viewModelScope.launch {
            preferencesRepo.setResetHour(hour)
        }
    }

    fun toggleTestMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setTestModeEnabled(enabled)
        }
    }

    fun setTestLimitMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepo.setTestLimitMinutes(minutes)
        }
    }

    fun resetTodayUsage() {
        viewModelScope.launch {
            usageRepo.resetTodayUsage()
        }
    }
}
