package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import com.mobile.tamatami.ui.screens.training.displayName
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LogWorkoutSheet(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (LocalDate, WorkoutType, Int, WorkoutIntensity, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(WorkoutType.STRENGTH) }
    var duration by remember { mutableStateOf("30") }
    var intensity by remember { mutableStateOf(WorkoutIntensity.MODERATE) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
        ) {
            Text("Log a workout", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            Text("Type", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutType.entries.forEach { t ->
                    FilterChip(
                        selected = t == type,
                        onClick = { type = t },
                        label = { Text(t.displayName()) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Duration", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 30, 45, 60).forEach { mins ->
                    FilterChip(
                        selected = duration == mins.toString(),
                        onClick = { duration = mins.toString() },
                        label = { Text("$mins min") },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Duration (minutes)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Text("Intensity", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WorkoutIntensity.entries.forEachIndexed { index, level ->
                    SegmentedButton(
                        selected = level == intensity,
                        onClick = { intensity = level },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = WorkoutIntensity.entries.size,
                        ),
                    ) { Text(level.displayName()) }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val mins = duration.toIntOrNull() ?: 0
                    if (mins > 0) {
                        onSave(date, type, mins, intensity, notes.ifBlank { null })
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
