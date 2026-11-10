package com.example.scrollsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.scrollsense.domain.model.LimitStatus
import com.example.scrollsense.ui.theme.StatusDanger
import com.example.scrollsense.ui.theme.StatusWarning

@Composable
fun UsageProgressBar(
    progress: Float,
    status: LimitStatus,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val progressColor = when (status) {
        LimitStatus.SAFE -> MaterialTheme.colorScheme.primary
        LimitStatus.NEAR_LIMIT -> StatusWarning
        LimitStatus.LIMIT_REACHED -> StatusDanger
    }

    val animatedColor by animateColorAsState(
        targetValue = progressColor,
        animationSpec = tween(durationMillis = 350),
        label = "progress_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(animatedColor)
        )
    }
}
