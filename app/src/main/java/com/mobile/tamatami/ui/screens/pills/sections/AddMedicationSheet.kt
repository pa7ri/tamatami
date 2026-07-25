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
import com.mobile.tamatami.domain.medication.TimeOfDay

/**
 * Add a medication: a name plus which [TimeOfDay] slots it's taken at. Mirrors
 * the [com.mobile.tamatami.ui.screens.training.sections.LogSleepSheet] modal
 * pattern. `onSave` receives the name and selected slots; the ViewModel derives
 * `dosesPerDay` and persists. Save is disabled until the name is non-blank and
 * at least one slot is chosen.
 */
@OptIn(ExperimentalMaterial3Api::class,
       androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddMedicationSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, slots: Set<TimeOfDay>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<TimeOfDay>() }

    val canSave = name.isNotBlank() && selected.isNotEmpty()

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

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(name.trim(), selected.toSet())
                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save medication") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
