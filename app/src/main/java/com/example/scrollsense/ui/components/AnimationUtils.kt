package com.example.scrollsense.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable press-scale and opacity micro-interaction for cards, buttons, and interactive elements.
 * Provides immediate physical tactile feedback (down to 0.97x) on press, smoothly restoring on release.
 */
@Composable
fun Modifier.pressEffect(
    scaleDown: Float = 0.97f,
    alphaDown: Float = 0.92f,
    interactionSource: MutableInteractionSource? = null
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "press_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) alphaDown else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "press_alpha"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * Staggered entrance animation for dashboard cards and screen sections.
 * Smoothly fades and slides vertically into position without layout recomposition overhead.
 */
@Composable
fun Modifier.staggeredEntrance(
    index: Int,
    baseDelayMs: Int = 45,
    durationMs: Int = 260,
    offsetDp: Float = 14f
): Modifier {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val delay = (index * baseDelayMs).coerceAtLeast(0)
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "entrance_alpha_$index"
    )
    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else offsetDp,
        animationSpec = tween(durationMillis = durationMs, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "entrance_transY_$index"
    )
    val density = LocalDensity.current

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = with(density) { translationY.dp.toPx() }
    }
}

/**
 * Animated number ticker for smooth numeric transitions (limits, counts).
 */
@Composable
fun AnimatedNumberText(
    value: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically(tween(160, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeIn(tween(160)))
                    .togetherWith(slideOutVertically(tween(120, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeOut(tween(120)))
            } else {
                (slideInVertically(tween(160, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeIn(tween(160)))
                    .togetherWith(slideOutVertically(tween(120, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeOut(tween(120)))
            }
        },
        label = "animated_number"
    ) { targetNum ->
        Text(
            text = "$targetNum$suffix",
            style = style,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1,
            softWrap = false,
            modifier = modifier
        )
    }
}
