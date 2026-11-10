package com.example.scrollsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.scrollsense.domain.model.ThemeMode
import com.example.scrollsense.ui.navigation.Routes
import com.example.scrollsense.ui.navigation.ScrollSenseAppRoot
import com.example.scrollsense.ui.theme.ScrollSenseTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ScrollSenseApp

        setContent {
            val prefs by app.preferencesRepository.preferencesFlow.collectAsState(
                initial = null
            )

            ScrollSenseTheme(
                themeMode = prefs?.themeMode ?: ThemeMode.DARK
            ) {
                if (prefs != null) {
                    val startRoute = if (prefs!!.isOnboardingCompleted) {
                        Routes.MAIN
                    } else {
                        Routes.ONBOARDING
                    }

                    ScrollSenseAppRoot(
                        startDestination = startRoute,
                        onCompleteOnboarding = {
                            lifecycleScope.launch {
                                app.preferencesRepository.setOnboardingCompleted(true)
                            }
                        }
                    )
                }
            }
        }
    }
}
