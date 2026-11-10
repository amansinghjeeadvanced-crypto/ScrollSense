package com.example.scrollsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollsense.domain.model.AttentionScore
import com.example.scrollsense.ui.theme.AccentIndigo
import com.example.scrollsense.ui.theme.StatusDanger
import com.example.scrollsense.ui.theme.StatusSafe
import com.example.scrollsense.ui.theme.StatusWarning

@Composable
fun ScoreGauge(
    score: AttentionScore,
    modifier: Modifier = Modifier
) {
    val targetSweep = if (score.isBuilding) 0.15f else (score.score / 100f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "score_progress"
    )

    val animatedScore by animateIntAsState(
        targetValue = score.score,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "score_text"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    val scoreColor = when {
        score.isBuilding -> primaryColor
        score.score >= 80 -> StatusSafe
        score.score >= 60 -> StatusWarning
        else -> StatusDanger
    }

    val animatedScoreColor by animateColorAsState(
        targetValue = scoreColor,
        animationSpec = tween(durationMillis = 400),
        label = "score_color"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val startAngle = 140f
            val maxSweepAngle = 260f

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = maxSweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (!score.isBuilding) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, AccentIndigo, animatedScoreColor)
                    ),
                    startAngle = startAngle,
                    sweepAngle = maxSweepAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                // Subtle pulse for building state
                drawArc(
                    color = primaryColor.copy(alpha = 0.4f),
                    startAngle = startAngle,
                    sweepAngle = maxSweepAngle * 0.25f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (score.isBuilding) {
                Text(
                    text = "Building",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Score",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "$animatedScore",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                    fontWeight = FontWeight.Bold,
                    color = animatedScoreColor
                )
                Text(
                    text = "ATTENTION SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
