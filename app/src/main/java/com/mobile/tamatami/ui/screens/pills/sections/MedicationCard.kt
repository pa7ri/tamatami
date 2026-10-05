package com.mobile.tamatami.ui.screens.pills.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.data.repository.MedicationToday
import com.mobile.tamatami.domain.medication.TimeOfDay

/**
 * One tracked medication: name, today's adherence, and a tappable chip per
 * scheduled [TimeOfDay] slot (selected = logged as taken today). Tapping a chip
 * toggles that slot; the trash icon removes the medication entirely.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MedicationCard(
    med: MedicationToday,
    onToggle: (slot: TimeOfDay, taken: Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val adherence = med.adherence
    // Show slots in a stable TimeOfDay order rather than set iteration order.
    val slots = TimeOfDay.entries.filter { it in med.scheduledSlots }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        med.medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = med.frequency.summary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when {
                            !med.dueToday -> "Not scheduled today"
                            adherence.complete -> "All done for today ✓"
                            else -> "${adherence.taken}/${adherence.expected} taken today"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete medication")
                }
            }

            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                slots.forEach { slot ->
                    val taken = slot in med.takenSlots
                    FilterChip(
                        selected = taken,
                        onClick = { onToggle(slot, !taken) },
                        label = { Text(slot.displayName) },
                    )
                }
            }
        }
    }
}
