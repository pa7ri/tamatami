package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/** Vertical bounce in dp-equivalents (-1f..0f). Multiply by dp.toPx() at draw. */
@Composable
fun rememberBounce(bounceHz: Float): State<Float> {
    val transition = rememberInfiniteTransition(label = "tama-bounce")
    val periodMs = remember(bounceHz) {
        ((1000f / bounceHz.coerceAtLeast(0.1f)).toInt()).coerceAtLeast(120)
    }
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tama-bounce-value",
    )
}

/** Eye openness: 1f means fully open, blinks down to 0.05f periodically. */
@Composable
fun rememberBlink(): State<Float> {
    val transition = rememberInfiniteTransition(label = "tama-blink")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3200
                1f at 0
                1f at 2800
                0.05f at 2960
                1f at 3120
            },
        ),
        label = "tama-blink-value",
    )
}

/**
 * A gentle rotation in degrees for accessory orbits (sparkles etc.).
 */
@Composable
fun rememberOrbit(durationMs: Int = 6000): State<Float> {
    val transition = rememberInfiniteTransition(label = "tama-orbit")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs),
        ),
        label = "tama-orbit-value",
    )
}
