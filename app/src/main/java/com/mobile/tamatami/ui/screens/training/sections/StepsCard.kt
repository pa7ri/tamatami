package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.data.health.StepDataSource
import com.mobile.tamatami.ui.screens.training.StepsUiState

/**
 * Today's step count from Health Connect, with graceful states when HC is
 * missing, needs an update, or hasn't been granted read access yet.
 */
@Composable
fun StepsCard(
    steps: StepsUiState,
    goal: Int,
    onConnect: () -> Unit,
    onInstallHealthConnect: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text(
                    "  Steps today",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(8.dp))

            when (steps) {
                StepsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                }
                is StepsUiState.Unavailable -> {
                    val msg = when (steps.reason) {
                        StepDataSource.Availability.UPDATE_REQUIRED ->
                            "Update Health Connect to sync your steps."
                        else -> "Install Health Connect to sync your steps."
                    }
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onInstallHealthConnect) {
                        Text(
                            if (steps.reason == StepDataSource.Availability.UPDATE_REQUIRED)
                                "Update Health Connect" else "Get Health Connect"
                        )
                    }
                }
                StepsUiState.NeedsPermission -> {
                    Text(
                        "Connect Health Connect to show your daily steps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onConnect) { Text("Connect steps") }
                }
                is StepsUiState.Ready -> {
                    Text(
                        text = steps.count?.let { "%,d".format(it) } ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (steps.count == null) "No step data yet today"
                        else "of ${"%,d".format(goal)} goal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (steps.count != null) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (steps.count.toFloat() / goal).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
