package com.mobile.tamatami.ui.screens.home.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.ui.screens.home.HomeUiState
import java.time.format.DateTimeFormatter

@Composable
fun NextPeriodSection(state: HomeUiState) {
    val predicted = state.cycle.predictedNextPeriod ?: return
    val days = state.cycle.daysUntilNextPeriod
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Next period",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    days == null -> predicted.format(formatter)
                    days <= 0 -> "Likely today"
                    days == 1 -> "In 1 day · ${predicted.format(formatter)}"
                    else -> "In $days days · ${predicted.format(formatter)}"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
