package com.example.scrollsense.ui.intervention

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.scrollsense.MainActivity
import com.example.scrollsense.ScrollSenseApp
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.domain.model.ThemeMode
import com.example.scrollsense.ui.reflection.ReflectionScreen
import com.example.scrollsense.ui.theme.ScrollSenseTheme
import com.example.scrollsense.util.TimeUtils
import kotlinx.coroutines.launch

class InterventionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_USED_MINUTES = "extra_used_minutes"
        const val EXTRA_LIMIT_MINUTES = "extra_limit_minutes"
        const val EXTRA_USED_VIDEOS = "extra_used_videos"
        const val EXTRA_LIMIT_VIDEOS = "extra_limit_videos"
        const val EXTRA_IS_VIDEO_LIMIT = "extra_is_video_limit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ProtectedApp.INSTAGRAM.packageName
        val usedMinutes = intent.getIntExtra(EXTRA_USED_MINUTES, 30)
        val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 30)
        val usedVideos = intent.getIntExtra(EXTRA_USED_VIDEOS, 50)
        val limitVideos = intent.getIntExtra(EXTRA_LIMIT_VIDEOS, 50)
        val isVideoLimit = intent.getBooleanExtra(EXTRA_IS_VIDEO_LIMIT, false)

        val protectedApp = ProtectedApp.fromPackageName(packageName) ?: ProtectedApp.INSTAGRAM
        val app = application as ScrollSenseApp

        setContent {
            val prefs by app.preferencesRepository.preferencesFlow.collectAsState(initial = null)

            ScrollSenseTheme(themeMode = prefs?.themeMode ?: ThemeMode.DARK) {
                var isReflecting by remember { mutableStateOf(false) }

                if (isReflecting) {
                    ReflectionScreen(
                        app = protectedApp,
                        onBack = { isReflecting = false },
                        onSaveReflection = { trigger, mood, note ->
                            lifecycleScope.launch {
                                app.usageRepository.recordReflection(
                                    app = protectedApp,
                                    trigger = trigger,
                                    mood = mood,
                                    notes = note
                                )
                                navigateHome()
                            }
                        }
                    )
                } else {
                    InterventionScreen(
                        app = protectedApp,
                        usedMinutes = usedMinutes,
                        limitMinutes = limitMinutes,
                        usedVideos = usedVideos,
                        limitVideos = limitVideos,
                        isVideoLimit = isVideoLimit,
                        resetTimeFormatted = TimeUtils.formatResetTime(0),
                        onGoBack = { navigateHome() },
                        onViewUsage = {
                            val mainIntent = Intent(this@InterventionActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(mainIntent)
                            finish()
                        },
                        onReflect = { isReflecting = true }
                    )
                }
            }
        }
    }

    private fun navigateHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
