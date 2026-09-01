package com.mobile.tamatami.ui.screens.pills.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.medication.FrequencyKind
import com.mobile.tamatami.domain.medication.MedicationFrequency
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.ui.screens.settings.sections.NumberStepperRow
import kotlinx.datetime.DayOfWeek

/**
 * Add a medication: a name, which [TimeOfDay] slots it's taken at, and how often
 * it's due ([MedicationFrequency] — daily / every N days / specific weekdays).
 * `onSave` receives the name, selected slots, and frequency; the ViewModel
 * derives `dosesPerDay` and persists. Save is disabled until the name is
 * non-blank and at least one slot is chosen.
 */
@OptIn(ExperimentalMaterial3Api::class,
       androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddMedicationSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, slots: Set<TimeOfDay>, frequency: MedicationFrequency) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<TimeOfDay>() }

    var kind by remember { mutableStateOf(FrequencyKind.DAILY) }
    var intervalDays by remember { mutableStateOf(2) }
    val weekdays = remember { mutableStateListOf<DayOfWeek>() }

    val canSave = name.isNotBlank() && selected.isNotEmpty() &&
        (kind != FrequencyKind.WEEKLY || weekdays.isNotEmpty())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Add medication", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (e.g. birth control, vitamin D)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text("When do you take it?", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeOfDay.entries.forEach { slot ->
                    val isOn = slot in selected
                    FilterChip(
                        selected = isOn,
                        onClick = { if (isOn) selected.remove(slot) else selected.add(slot) },
                        label = { Text(slot.displayName) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("How often?", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FrequencyChip("Every day", kind == FrequencyKind.DAILY) {
                    kind = FrequencyKind.DAILY
                }
                FrequencyChip("Every N days", kind == FrequencyKind.EVERY_N_DAYS) {
                    kind = FrequencyKind.EVERY_N_DAYS
                }
                FrequencyChip("Weekly", kind == FrequencyKind.WEEKLY) {
                    kind = FrequencyKind.WEEKLY
                }
            }

            when (kind) {
                FrequencyKind.EVERY_N_DAYS -> {
                    Spacer(Modifier.height(8.dp))
                    NumberStepperRow(
                        label = "Interval",
                        suffix = "days",
                        value = intervalDays,
                        range = 2..30,
                        onChange = { intervalDays = it },
                    )
                }
                FrequencyKind.WEEKLY -> {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayOfWeek.entries.forEach { day ->
                            val isOn = day in weekdays
                            FilterChip(
                                selected = isOn,
                                onClick = { if (isOn) weekdays.remove(day) else weekdays.add(day) },
                                label = { Text(day.shortLabel()) },
                            )
                        }
                    }
                }
                FrequencyKind.DAILY -> Unit
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(name.trim(), selected.toSet(), buildFrequency(kind, intervalDays, weekdays.toSet()))
                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save medication") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrequencyChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

private fun buildFrequency(
    kind: FrequencyKind,
    intervalDays: Int,
    weekdays: Set<DayOfWeek>,
): MedicationFrequency = when (kind) {
    FrequencyKind.DAILY -> MedicationFrequency.Daily
    FrequencyKind.EVERY_N_DAYS -> MedicationFrequency(kind, intervalDays = intervalDays)
    FrequencyKind.WEEKLY -> MedicationFrequency(kind, weekdays = weekdays)
}

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
    else -> name.take(3)
}
