package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.TamaExpression
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.model.TamagotchiState
import com.mobile.tamatami.ui.tamagotchi.TamagotchiAvatar
import com.mobile.tamatami.ui.theme.InstagramBrush

@Composable
fun OnboardingShell(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    body: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InstagramBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // systemBarsPadding keeps content clear of BOTH the status bar
                // (top) and the navigation bar (bottom); the gradient background
                // on the outer Box still bleeds edge-to-edge behind them.
                .systemBarsPadding()
                .padding(24.dp),
        ) {
            if (onBack != null) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                ) { Text("Back") }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                body()
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF962FBF),
                ),
                shape = RoundedCornerShape(50),
            ) {
                Text(primaryLabel)
            }
        }
    }
}

@Composable
internal fun OnboardingTamaPreview(
    expression: TamaExpression = TamaExpression.HAPPY,
) {
    Box(modifier = Modifier.size(220.dp)) {
        TamagotchiAvatar(
            state = TamagotchiState(
                mood = TamagotchiMood.HAPPY,
                accessories = emptySet(),
                bounceHz = 0.8f,
                expression = expression,
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
