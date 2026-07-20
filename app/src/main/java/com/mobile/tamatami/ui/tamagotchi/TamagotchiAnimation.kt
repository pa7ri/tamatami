package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/**
 * Vertical bounce in dp-equivalents (`-1f..0f`, 0 = grounded). Multiply by
 * `dp.toPx()` at draw time and apply as `translationY`.
 */
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

/**
 * Squash-and-stretch factor (`0f..1f`) synced to the bounce, a touch out of
 * phase so the cat stretches on the way up and squashes on the way down —
 * reads as a lively bounce rather than a rigid float. Map to scaleX/scaleY at
 * the call site (e.g. `scaleY = 1 + squash*0.06`, `scaleX = 1 - squash*0.06`).
 */
@Composable
fun rememberSquash(bounceHz: Float): State<Float> {
    val transition = rememberInfiniteTransition(label = "tama-squash")
    // Half the bounce period: one squash per up-and-down would be too slow, so
    // we run at the landing cadence.
    val periodMs = remember(bounceHz) {
        ((500f / bounceHz.coerceAtLeast(0.1f)).toInt()).coerceAtLeast(90)
    }
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tama-squash-value",
    )
}

/** Eye openness: 1f fully open, blinks down to ~0.05f periodically. */
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
 * Tail sway phase in `-1f..1f`, a continuous back-and-forth. Amplitude is
 * applied at draw time by scaling with the mood's `tailWag`, so a still cat
 * (tailWag≈0) barely moves and a happy one swings fully.
 */
@Composable
fun rememberTailPhase(): State<Float> {
    val transition = rememberInfiniteTransition(label = "tama-tail")
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tama-tail-value",
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
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
        ),
        label = "tama-orbit-value",
    )
}
