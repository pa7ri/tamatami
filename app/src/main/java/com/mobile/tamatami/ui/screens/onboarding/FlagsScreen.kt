package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FlagsScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val draft by viewModel.draft.collectAsState()
    OnboardingShell(
        title = "A few quick questions",
        subtitle = "These help your Tama give better tips. All optional.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = onNext,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            FlagRow(
                label = "I'm trying to conceive",
                checked = draft.tryingToConceive,
                onCheckedChange = viewModel::setTryingToConceive,
            )
            Spacer(Modifier.height(12.dp))
            FlagRow(
                label = "I'm on hormonal contraception",
                checked = draft.onContraception,
                onCheckedChange = viewModel::setOnContraception,
            )
            Spacer(Modifier.height(12.dp))
            FlagRow(
                label = "My cycles are irregular",
                checked = draft.irregularCycles,
                onCheckedChange = viewModel::setIrregularCycles,
            )
        }
    }
}

@Composable
private fun FlagRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color(0xFF962FBF),
                ),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = label, color = Color.White)
        }
    }
}
