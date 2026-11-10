package com.example.scrollsense.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scrollsense.domain.model.DailyAppUsage
import com.example.scrollsense.domain.model.InsightType
import com.example.scrollsense.domain.model.LimitStatus
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.ui.components.GlowCard
import com.example.scrollsense.ui.components.MetricMiniItem
import com.example.scrollsense.ui.components.ProtectionStatusBanner
import com.example.scrollsense.ui.components.ScoreGauge
import com.example.scrollsense.ui.components.UsageProgressBar
import com.example.scrollsense.ui.components.pressEffect
import com.example.scrollsense.ui.components.staggeredEntrance
import com.example.scrollsense.ui.theme.AccentIndigo
import com.example.scrollsense.ui.theme.StatusDanger
import com.example.scrollsense.ui.theme.StatusSafe
import com.example.scrollsense.ui.theme.StatusSafeBg
import com.example.scrollsense.ui.theme.StatusWarning
import com.example.scrollsense.ui.theme.StatusWarningBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigatePermissions: () -> Unit,
    onNavigateLimits: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshUsage()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshUsage() },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header: Responsive Greeting & Quick Refresh (Animated entrance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${uiState.greeting}, ${uiState.userName}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Protecting your daily attention",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isDemoMode) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DEMO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else if (uiState.isTestMode) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusWarningBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TEST MODE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshUsage() },
                        modifier = Modifier.pressEffect()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Protection Status Banner (Staggered entrance)
            Box(modifier = Modifier.staggeredEntrance(1)) {
                ProtectionStatusBanner(
                    permissionStatus = uiState.permissionStatus,
                    onNavigatePermissions = onNavigatePermissions
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Attention Score Card (Staggered entrance)
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(2)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScoreGauge(score = uiState.attentionScore)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.attentionScore.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Metric Badges - Responsive with equal weights
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricMiniItem(
                            label = "Adherence",
                            value = "${uiState.attentionScore.adherencePercent}%",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        MetricMiniItem(
                            label = "Late-Night",
                            value = "${uiState.attentionScore.lateNightMinutes}m",
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        MetricMiniItem(
                            label = "Interventions",
                            value = "${uiState.attentionScore.totalInterventions}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Limits Section Header (Staggered entrance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Limits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
                TextButton(
                    onClick = onNavigateLimits,
                    modifier = Modifier.pressEffect()
                ) {
                    Text(
                        text = "Edit Limits",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Unified Short-Form Daily Counter Card - Fully responsive (Staggered entrance)
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(4),
                onClick = onNavigateLimits
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Short videos today: ${uiState.unifiedShortVideosCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Daily limit: ${uiState.unifiedShortVideosLimit} videos (Shared)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val rawProgress = (uiState.unifiedShortVideosCount.toFloat() / uiState.unifiedShortVideosLimit.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val animatedProgressPercent by animateIntAsState(
                        targetValue = (rawProgress * 100).toInt(),
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        label = "short_videos_progress"
                    )
                    Text(
                        text = "$animatedProgressPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rawProgress >= 1f) StatusDanger else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Protected App Usage Cards (Staggered entrance)
            uiState.appUsageList.forEachIndexed { index, usage ->
                Box(modifier = Modifier.staggeredEntrance(5 + index)) {
                    AppUsageCard(usage = usage, onCardClick = onNavigateLimits)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Today's Insight Card (Staggered entrance)
            GlowCard(
                modifier = Modifier.staggeredEntrance(5 + uiState.appUsageList.size),
                borderColor = when (uiState.dailyInsight.type) {
                    InsightType.POSITIVE -> StatusSafe.copy(alpha = 0.3f)
                    InsightType.WARNING -> StatusWarning.copy(alpha = 0.3f)
                    InsightType.NEUTRAL -> MaterialTheme.colorScheme.outline
                }
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState.dailyInsight.type) {
                                    InsightType.POSITIVE -> StatusSafeBg
                                    InsightType.WARNING -> StatusWarningBg
                                    InsightType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = when (uiState.dailyInsight.type) {
                                InsightType.POSITIVE -> StatusSafe
                                InsightType.WARNING -> StatusWarning
                                InsightType.NEUTRAL -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.dailyInsight.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.dailyInsight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AppUsageCard(
    usage: DailyAppUsage,
    onCardClick: () -> Unit
) {
    val statusColor = when (usage.status) {
        LimitStatus.SAFE -> StatusSafe
        LimitStatus.NEAR_LIMIT -> StatusWarning
        LimitStatus.LIMIT_REACHED -> StatusDanger
    }

    val statusBg = when (usage.status) {
        LimitStatus.SAFE -> StatusSafeBg
        LimitStatus.NEAR_LIMIT -> StatusWarningBg
        LimitStatus.LIMIT_REACHED -> StatusDanger.copy(alpha = 0.15f)
    }

    val statusText = when (usage.status) {
        LimitStatus.SAFE -> "Safe"
        LimitStatus.NEAR_LIMIT -> "Near limit"
        LimitStatus.LIMIT_REACHED -> "Limit reached"
    }

    GlowCard(onClick = onCardClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (usage.app == ProtectedApp.INSTAGRAM) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else AccentIndigo.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (usage.app == ProtectedApp.INSTAGRAM) "IG" else "YT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (usage.app == ProtectedApp.INSTAGRAM) MaterialTheme.colorScheme.primary else AccentIndigo
                    )
                }

                Column {
                    Text(
                        text = usage.app.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Enforced: ${usage.usedMinutes} / ${usage.limitMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Pill - Stays on one line
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar (Smooth animated sweep)
        UsageProgressBar(
            progress = usage.progress,
            status = usage.status
        )

        if (usage.interventionsCount > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${usage.interventionsCount} intervention${if (usage.interventionsCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusDanger,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
