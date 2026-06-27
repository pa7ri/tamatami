package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingSummaryScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit,
    onBack: () -> Unit,
) {
    val draft by viewModel.draft.collectAsState()
    OnboardingShell(
        title = "Ready to hatch ${draft.tamaName}?",
        subtitle = "Check the details below.",
        onBack = onBack,
        primaryLabel = "Hatch my Tama",
        onPrimary = { viewModel.finish(onFinish) },
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SummaryRow("Tama's name", draft.tamaName)
                Spacer(Modifier.height(8.dp))
                SummaryRow("Last period", draft.lastPeriodStart.toString())
                Spacer(Modifier.height(8.dp))
                SummaryRow("Cycle length", "${draft.avgCycleLengthDays} days")
                Spacer(Modifier.height(8.dp))
                SummaryRow("Period length", "${draft.avgPeriodLengthDays} days")
                if (draft.tryingToConceive || draft.onContraception || draft.irregularCycles) {
                    Spacer(Modifier.height(8.dp))
                    val flags = buildList {
                        if (draft.tryingToConceive) add("trying to conceive")
                        if (draft.onContraception) add("on contraception")
                        if (draft.irregularCycles) add("irregular cycles")
                    }.joinToString(", ")
                    SummaryRow("Notes", flags)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
