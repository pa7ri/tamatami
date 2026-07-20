package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.Accessory
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.model.TamagotchiState
import com.mobile.tamatami.ui.theme.LocalMoodColors
import com.mobile.tamatami.ui.theme.TamatamiClassicTheme

private const val MOOD_TRANSITION_MS = 420

/**
 * Tamagotchi mascot — a pixel-art orange tabby cat. Stateless: everything
 * visual is driven from [state].
 *
 * The fur is a fixed tabby palette; **mood is expressed through the face and
 * pose** ([CatExpression]) plus a soft mood-tinted aura behind the cat. Every
 * expression field and the aura color are interpolated, so a mood change eases
 * over [MOOD_TRANSITION_MS] rather than snapping. The whole cat bounces with a
 * subtle squash-and-stretch, blinks, and wags its tail (more when happier).
 */
@Composable
fun TamagotchiAvatar(
    state: TamagotchiState,
    modifier: Modifier = Modifier,
) {
    val moodColors = LocalMoodColors.current
    val bounce by rememberBounce(state.bounceHz)
    val squash by rememberSquash(state.bounceHz)
    val blink by rememberBlink()
    val tailPhase by rememberTailPhase()
    val density = LocalDensity.current
    val bouncePx = with(density) { 10.dp.toPx() }

    // Interpolate each expression field toward the current mood's target so the
    // face eases between moods instead of snapping.
    val target = state.mood.expression()
    val spec = tween<Float>(MOOD_TRANSITION_MS, easing = FastOutSlowInEasing)
    val eyeOpen by animateFloatAsState(target.eyeOpen, spec, label = "eyeOpen")
    val mouthCurve by animateFloatAsState(target.mouthCurve, spec, label = "mouthCurve")
    val earPerk by animateFloatAsState(target.earPerk, spec, label = "earPerk")
    val cheekBlush by animateFloatAsState(target.cheekBlush, spec, label = "cheekBlush")
    val tailWag by animateFloatAsState(target.tailWag, spec, label = "tailWag")
    val sparkle by animateFloatAsState(target.sparkle, spec, label = "sparkle")
    val expr = CatExpression(eyeOpen, mouthCurve, earPerk, cheekBlush, tailWag, sparkle)

    val auraColor by animateColorAsState(
        targetValue = moodColors.colorFor(state.mood),
        animationSpec = tween(MOOD_TRANSITION_MS, easing = FastOutSlowInEasing),
        label = "aura",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "tama-${state.mood.name.lowercase()}" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = bounce * bouncePx
                    // Squash on landing (bounce near 0), stretch at apex.
                    val s = squash * 0.06f
                    scaleX = 1f - s
                    scaleY = 1f + s
                },
        ) {
            // Aura: always present, brightest for glowing moods (sparkle drives it).
            drawAura(size, auraColor, intensity = 0.5f + 0.5f * expr.sparkle)
            drawPixelCat(
                canvasSize = size,
                expr = expr,
                blink = blink,
                tailPhase = tailPhase * expr.tailWag,
            )
        }

        AccessoryOverlay(state.accessories)
    }
}

@Composable
private fun AccessoryOverlay(accessories: Set<Accessory>) {
    if (accessories.isEmpty()) return
    Box(modifier = Modifier.fillMaxSize()) {
        accessories.forEach { accessory ->
            val (alignment, tint, icon, desc) = accessoryConfig(accessory)
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = alignment,
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = desc,
                        tint = tint,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.95f),
                    )
                }
            }
        }
    }
}

private data class AccessoryConfig(
    val alignment: Alignment,
    val tint: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val desc: String,
)

private fun accessoryConfig(accessory: Accessory): AccessoryConfig = when (accessory) {
    Accessory.THIRSTY_DROPLET -> AccessoryConfig(
        Alignment.TopEnd, Color(0xFF2D8DD9), Icons.Filled.WaterDrop, "thirsty"
    )
    Accessory.TIRED_ZZZ -> AccessoryConfig(
        Alignment.TopStart, Color(0xFF6E5A8A), Icons.Filled.Bedtime, "tired"
    )
    Accessory.HEART -> AccessoryConfig(
        Alignment.BottomEnd, Color(0xFFD62976), Icons.Filled.Favorite, "loved"
    )
    Accessory.SPARKLE -> AccessoryConfig(
        Alignment.TopStart, Color(0xFFCFA73C), Icons.Filled.AutoAwesome, "sparkle"
    )
    Accessory.BANDAID -> AccessoryConfig(
        Alignment.BottomStart, Color(0xFFE4527A), Icons.Filled.Healing, "cared-for"
    )
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

@Preview(name = "Sad", showBackground = true)
@Composable
private fun PreviewSad() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(
            mood = TamagotchiMood.SAD,
            accessories = setOf(Accessory.THIRSTY_DROPLET, Accessory.TIRED_ZZZ),
            bounceHz = 0.6f,
        ),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Neutral", showBackground = true)
@Composable
private fun PreviewNeutral() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.NEUTRAL, emptySet(), 0.6f),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Content", showBackground = true)
@Composable
private fun PreviewContent() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.CONTENT, setOf(Accessory.BANDAID), 0.6f),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Happy", showBackground = true)
@Composable
private fun PreviewHappy() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.HAPPY, setOf(Accessory.HEART), 0.6f),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Glowing", showBackground = true)
@Composable
private fun PreviewGlowing() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.GLOWING, setOf(Accessory.SPARKLE), 1.4f),
        modifier = Modifier.size(220.dp),
    )
}
