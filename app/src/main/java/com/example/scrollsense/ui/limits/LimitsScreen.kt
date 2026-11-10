package com.example.scrollsense.ui.limits

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scrollsense.domain.model.ProtectedApp
import com.example.scrollsense.ui.components.AnimatedNumberText
import com.example.scrollsense.ui.components.GlowCard
import com.example.scrollsense.ui.components.pressEffect
import com.example.scrollsense.ui.components.staggeredEntrance
import com.example.scrollsense.ui.theme.AccentIndigo
import com.example.scrollsense.ui.theme.StatusWarning
import com.example.scrollsense.ui.theme.StatusWarningBg
import com.example.scrollsense.util.TimeUtils

@Composable
fun LimitsScreen(
    viewModel: LimitsViewModel = viewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Title (Staggered entrance)
        Column(modifier = Modifier.staggeredEntrance(0)) {
            Text(
                text = "Daily Boundaries",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Configure limits for short-form feeds and apps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Unified Short Videos Daily Limit Card (Shared across YouTube Shorts & Instagram Reels)
        Box(modifier = Modifier.staggeredEntrance(1)) {
            UnifiedShortVideosLimitCard(
                currentLimit = prefs.unifiedShortVideosLimit,
                onLimitChanged = { viewModel.updateUnifiedShortVideosLimit(it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "App Time Allowances",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.staggeredEntrance(2)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Instagram Time Allowance Card
        Box(modifier = Modifier.staggeredEntrance(3)) {
            AppTimeLimitCard(
                app = ProtectedApp.INSTAGRAM,
                currentMinutes = prefs.instagramLimitMinutes,
                isEnabled = prefs.instagramEnabled,
                onConfigChanged = { mins, enabled ->
                    viewModel.updateInstagramLimit(mins, prefs.instagramVideoTarget, enabled)
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. YouTube Time Allowance Card
        Box(modifier = Modifier.staggeredEntrance(4)) {
            AppTimeLimitCard(
                app = ProtectedApp.YOUTUBE,
                currentMinutes = prefs.youtubeLimitMinutes,
                isEnabled = prefs.youtubeEnabled,
                onConfigChanged = { mins, enabled ->
                    viewModel.updateYouTubeLimit(mins, prefs.youtubeVideoTarget, enabled)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Daily Reset Time Card
        GlowCard(modifier = Modifier.staggeredEntrance(5)) {
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Daily Reset Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "Counters refresh at ${TimeUtils.formatResetTime(prefs.resetHour)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Responsive horizontally scrollable chip row: chips never squeeze on small screens
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(0 to "Midnight", 3 to "3:00 AM", 6 to "6:00 AM").forEach { (hour, label) ->
                    val isSelected = prefs.resetHour == hour
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateResetHour(hour) },
                        modifier = Modifier.pressEffect(),
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Test Mode Card
        GlowCard(
            modifier = Modifier.staggeredEntrance(6),
            borderColor = if (prefs.isTestModeEnabled) StatusWarning else MaterialTheme.colorScheme.outline,
            backgroundColor = if (prefs.isTestModeEnabled) StatusWarningBg else MaterialTheme.colorScheme.surface
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (prefs.isTestModeEnabled) StatusWarning.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                            tint = if (prefs.isTestModeEnabled) StatusWarning else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Test Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "Test interventions with 3 videos & 1–3 min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Switch(
                    checked = prefs.isTestModeEnabled,
                    onCheckedChange = { viewModel.toggleTestMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StatusWarning,
                        checkedTrackColor = StatusWarning.copy(alpha = 0.3f)
                    )
                )
            }

            if (prefs.isTestModeEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Active Test Video Limit: 3 short videos (Intervention on 3rd swipe)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusWarning
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable chips so test limits never wrap awkwardly
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    listOf(1, 2, 3).forEach { testMins ->
                        val isSelected = prefs.testLimitMinutes == testMins
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTestLimitMinutes(testMins) },
                            modifier = Modifier.pressEffect(),
                            label = {
                                Text(
                                    text = "${testMins}m limit",
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusWarning,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.resetTodayUsage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressEffect()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = null,
                        tint = StatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Today's Test Data",
                        color = StatusWarning,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Technical Notice
        GlowCard(
            modifier = Modifier.staggeredEntrance(7),
            padding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            borderColor = MaterialTheme.colorScheme.outline
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Unified Counter Architecture",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ScrollSense monitors short-form feeds across YouTube Shorts and Instagram Reels using an in-memory transition state machine backed by persistent storage. When your daily short videos limit is reached, an intervention automatically triggers.",
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

@Composable
fun UnifiedShortVideosLimitCard(
    currentLimit: Int,
    onLimitChanged: (limit: Int) -> Unit
) {
    var limit by remember(currentLimit) { mutableFloatStateOf(currentLimit.toFloat()) }

    GlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
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
                        text = "Daily Short Videos Limit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Shared counter: YouTube Shorts + Instagram Reels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Maximum Short Videos / Day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedNumberText(
                value = limit.toInt(),
                suffix = " videos",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = limit,
            onValueChange = { limit = it },
            onValueChangeFinished = { onLimitChanged(limit.toInt()) },
            valueRange = 10f..200f,
            steps = 37, // increments of 5
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Horizontally scrollable chip row: chips never squeeze on small screens
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            listOf(25, 50, 75, 100).forEach { preset ->
                val isSelected = limit.toInt() == preset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        limit = preset.toFloat()
                        onLimitChanged(preset)
                    },
                    modifier = Modifier.pressEffect(),
                    label = {
                        Text(
                            text = "$preset videos",
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun AppTimeLimitCard(
    app: ProtectedApp,
    currentMinutes: Int,
    isEnabled: Boolean,
    onConfigChanged: (minutes: Int, enabled: Boolean) -> Unit
) {
    var minutes by remember(currentMinutes) { mutableFloatStateOf(currentMinutes.toFloat()) }

    GlowCard {
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
                        .background(
                            if (app == ProtectedApp.INSTAGRAM) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else AccentIndigo.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app == ProtectedApp.INSTAGRAM) "IG" else "YT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (app == ProtectedApp.INSTAGRAM) MaterialTheme.colorScheme.primary else AccentIndigo
                    )
                }

                Column {
                    Text(
                        text = app.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (isEnabled) "${minutes.toInt()} minutes / day" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onConfigChanged(minutes.toInt(), it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Time Allowance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedNumberText(
                    value = minutes.toInt(),
                    suffix = " min",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = minutes,
                onValueChange = { minutes = it },
                onValueChangeFinished = {
                    onConfigChanged(minutes.toInt(), isEnabled)
                },
                valueRange = 5f..180f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Fixed: Horizontally scrollable chip row: "90m" never wraps into separate characters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(15, 30, 45, 60, 90).forEach { presetMins ->
                    val isSelected = minutes.toInt() == presetMins
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            minutes = presetMins.toFloat()
                            onConfigChanged(presetMins, isEnabled)
                        },
                        modifier = Modifier.pressEffect(),
                        label = {
                            Text(
                                text = "${presetMins}m",
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}
