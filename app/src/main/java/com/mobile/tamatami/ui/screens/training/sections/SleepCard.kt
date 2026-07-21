package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.SleepSummary
import com.mobile.tamatami.domain.sleep.ExpectedSleepPredictor
import com.mobile.tamatami.domain.sleep.SleepQuality

/** Last night's sleep: duration + estimated quality, or a prompt to log it. */
@Composable
fun SleepCard(
    sleep: SleepSummary?,
    goalMinutes: Int,
    expected: ExpectedSleepPredictor.Prediction?,
    onLogSleep: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sleep", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))

            if (sleep == null) {
                Text(
                    "No sleep logged for last night.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDuration(sleep.durationMinutes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(12.dp))
                    QualityPill(sleep.quality)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatClock(sleep.bedMinuteOfDay)} → ${formatClock(sleep.wakeMinuteOfDay)} " +
                        "· goal ${formatDuration(goalMinutes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Forward-looking prediction from recent history.
            if (expected != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "You usually sleep ${qualityLabel(expected.expected).lowercase()} " +
                        "(based on ${expected.basedOnNights} nights)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onLogSleep) {
                Text(if (sleep == null) "Log sleep" else "Update sleep")
            }
        }
    }
}

@Composable
private fun QualityPill(quality: SleepQuality) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = qualityLabel(quality),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

private fun qualityLabel(quality: SleepQuality): String = when (quality) {
    SleepQuality.POOR -> "Poor"
    SleepQuality.FAIR -> "Fair"
    SleepQuality.GOOD -> "Good"
    SleepQuality.GREAT -> "Great"
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}

private fun formatClock(minuteOfDay: Int): String {
    val h = (minuteOfDay / 60) % 24
    val m = minuteOfDay % 60
    return "%02d:%02d".format(h, m)
}
