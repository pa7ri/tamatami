package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PeriodLengthScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val draft by viewModel.draft.collectAsState()
    OnboardingShell(
        title = "How long does your period typically last?",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = onNext,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = "${draft.avgPeriodLengthDays} days",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            Slider(
                value = draft.avgPeriodLengthDays.toFloat(),
                onValueChange = { viewModel.setPeriodLength(it.toInt()) },
                valueRange = 2f..10f,
                steps = 10 - 2 - 1,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.4f),
                ),
            )
        }
    }
}
