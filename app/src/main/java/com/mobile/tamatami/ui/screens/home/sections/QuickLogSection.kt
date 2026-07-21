package com.mobile.tamatami.ui.screens.home.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.ui.screens.home.HomeUiState

@Composable
fun QuickLogSection(
    state: HomeUiState,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    onMoodSelected: (Mood) -> Unit,
    onFlowSelected: (PeriodFlow) -> Unit,
) {
    var moodOpen by remember { mutableStateOf(false) }
    var flowOpen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Quick log",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            WaterTile(
                value = "${state.daily.waterGlasses}/${state.waterGoal}",
                onAdd = onAddWater,
                onRemove = onRemoveWater,
                modifier = Modifier.weight(1f),
            )
            QuickLogTile(
                title = "Mood",
                value = state.daily.mood?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—",
                helper = "Tap to set",
                onClick = { moodOpen = true },
                modifier = Modifier.weight(1f),
            )
            QuickLogTile(
                title = "Flow",
                value = state.daily.periodFlow?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—",
                helper = "Tap to set",
                onClick = { flowOpen = true },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (moodOpen) {
        ChoiceDialog(
            title = "How are you feeling?",
            options = Mood.entries.map { it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPicked = {
                onMoodSelected(it)
                moodOpen = false
            },
            onDismiss = { moodOpen = false },
        )
    }
    if (flowOpen) {
        ChoiceDialog(
            title = "Period flow",
            options = PeriodFlow.entries.map { it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            onPicked = {
                onFlowSelected(it)
                flowOpen = false
            },
            onDismiss = { flowOpen = false },
        )
    }
}

@Composable
private fun WaterTile(
    value: String,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Water", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp),
                ) { Icon(Icons.Outlined.Remove, contentDescription = "Remove a glass") }
                FilledTonalIconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(32.dp),
                ) { Icon(Icons.Outlined.Add, contentDescription = "Add a glass") }
            }
        }
    }
}

@Composable
private fun QuickLogTile(
    title: String,
    value: String,
    helper: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    onPicked: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPicked(value) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
