package com.example.scrollsense.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrollsense.ui.analytics.AnalyticsScreen
import com.example.scrollsense.ui.home.HomeScreen
import com.example.scrollsense.ui.limits.LimitsScreen
import com.example.scrollsense.ui.onboarding.OnboardingScreen
import com.example.scrollsense.ui.permissions.PermissionsScreen
import com.example.scrollsense.ui.settings.SettingsScreen
@Composable
fun ScrollSenseAppRoot(
    startDestination: String,
    onCompleteOnboarding: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) },
        popEnterTransition = { fadeIn(tween(220, easing = FastOutSlowInEasing)) },
        popExitTransition = { fadeOut(tween(180, easing = FastOutSlowInEasing)) }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    onCompleteOnboarding()
                    navController.navigate(Routes.PERMISSIONS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onContinue = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScaffold(
                onNavigatePermissions = {
                    navController.navigate(Routes.PERMISSIONS)
                }
            )
        }
    }
}

@Composable
fun MainScaffold(
    onNavigatePermissions: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavItem.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavItem.entries.forEach { item ->
                    val isSelected = selectedTab == item

                    // Subtle micro-scale animation on the selected tab icon
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "nav_icon_scale"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Smooth directional transition when switching tabs
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val offset = if (forward) 36 else -36
                    (slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { offset } +
                            fadeIn(tween(220, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            slideOutHorizontally(animationSpec = tween(160, easing = FastOutSlowInEasing)) { -offset } +
                                    fadeOut(tween(160, easing = FastOutSlowInEasing))
                        )
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    NavItem.HOME -> HomeScreen(
                        onNavigatePermissions = onNavigatePermissions,
                        onNavigateLimits = { selectedTab = NavItem.LIMITS }
                    )
                    NavItem.ANALYTICS -> AnalyticsScreen()
                    NavItem.LIMITS -> LimitsScreen()
                    NavItem.SETTINGS -> SettingsScreen(
                        onNavigatePermissions = onNavigatePermissions
                    )
                }
            }
        }
    }
}
