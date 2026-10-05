package com.mobile.tamatami.ui.tamagotchi

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mobile.tamatami.domain.model.TamaExpression
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.model.TamagotchiState
import com.mobile.tamatami.ui.theme.TamatamiClassicTheme

private const val EXPRESSION_CROSSFADE_MS = 420

/**
 * Tamagotchi mascot, rendered as a looping Lottie animation. Stateless:
 * [TamagotchiState.expression] selects which of the seven animations plays.
 * When the expression changes (the underlying cycle/log signals shift), the
 * new animation cross-fades in over [EXPRESSION_CROSSFADE_MS].
 *
 * Animations live in `assets/tama/` — see [assetFor].
 */
@Composable
fun TamagotchiAvatar(
    state: TamagotchiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "tama-${state.expression.name.lowercase()}" },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = state.expression,
            animationSpec = tween(EXPRESSION_CROSSFADE_MS),
            label = "tama-expression",
        ) { expression ->
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Asset(assetFor(expression)),
            )
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Maps an [TamaExpression] to its bundled Lottie asset. `LottieCompositionSpec.Asset`
 * accepts both dotLottie (`.lottie`) and bare Lottie `.json`; every expression uses the
 * dotLottie exports bundled in `assets/tama/`.
 */
internal fun assetFor(expression: TamaExpression): String = "tama/" + when (expression) {
    TamaExpression.GREETING -> "greeting.lottie"
    TamaExpression.HAPPY -> "happy.lottie"
    TamaExpression.SAD_TIRED -> "sad_tired.lottie"
    TamaExpression.MOODY -> "moody.lottie"
    TamaExpression.ROMANTIC -> "romantic.lottie"
}

// -----------------------------------------------------------------------------
// Previews (the Lottie asset won't render in the IDE preview, but these keep the
// composable and each expression mapping compiling).
// -----------------------------------------------------------------------------

@Preview(name = "Greeting", showBackground = true)
@Composable
private fun PreviewGreeting() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.NEUTRAL, emptySet(), 0.6f, TamaExpression.GREETING),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Happy", showBackground = true)
@Composable
private fun PreviewHappy() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.HAPPY, emptySet(), 1.4f, TamaExpression.HAPPY),
        modifier = Modifier.size(220.dp),
    )
}

@Preview(name = "Romantic", showBackground = true)
@Composable
private fun PreviewRomantic() = TamatamiClassicTheme {
    TamagotchiAvatar(
        TamagotchiState(TamagotchiMood.GLOWING, emptySet(), 1.4f, TamaExpression.ROMANTIC),
        modifier = Modifier.size(220.dp),
    )
}
