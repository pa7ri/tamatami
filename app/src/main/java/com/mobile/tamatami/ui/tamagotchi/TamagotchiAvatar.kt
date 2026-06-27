package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.Accessory
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.model.TamagotchiState
import com.mobile.tamatami.ui.theme.IgYellow
import com.mobile.tamatami.ui.theme.LocalMoodColors
import com.mobile.tamatami.ui.theme.TamatamiClassicTheme
import androidx.compose.foundation.layout.Column

/**
 * Tamagotchi mascot. Stateless: everything visual is driven from [state].
 *
 * Renders body, cheeks, eyes, mouth, optional glow, and a layer of icon
 * "accessories" on top of the canvas. The whole avatar bounces along the
 * Y axis at `state.bounceHz`.
 */
@Composable
fun TamagotchiAvatar(
    state: TamagotchiState,
    modifier: Modifier = Modifier,
) {
    val moodColors = LocalMoodColors.current
    val bounce = rememberBounce(state.bounceHz)
    val blink = rememberBlink()
    val density = LocalDensity.current
    val bouncePx = with(density) { 8.dp.toPx() }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "tama-${state.mood.name.lowercase()}" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = bounce.value * bouncePx },
        ) {
            if (state.mood == TamagotchiMood.GLOWING) {
                drawGlow(size, IgYellow)
            }
            drawTamaBody(
                bodyColor = moodColors.colorFor(state.mood),
                shadeColor = moodColors.shadeFor(state.mood),
                canvasSize = size,
            )
            val cheekAlpha = when (state.mood) {
                TamagotchiMood.SAD, TamagotchiMood.NEUTRAL -> 0f
                TamagotchiMood.CONTENT -> 0.4f
                TamagotchiMood.HAPPY -> 0.7f
                TamagotchiMood.GLOWING -> 0.85f
            }
            drawCheeks(size, cheekAlpha)
            drawEyes(state.mood, blink.value, size)
            drawMouth(state.mood, size)
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
