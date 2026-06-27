package com.mobile.tamatami.ui.screens.hormones.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.mobile.tamatami.domain.hormones.HormoneMarker
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddEntrySheet(
    today: LocalDate,
    initialMarker: HormoneMarker,
    onDismiss: () -> Unit,
    onSave: (LocalDate, HormoneMarker, Float, String, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var marker by remember { mutableStateOf(initialMarker) }
    var dropdownOpen by remember { mutableStateOf(false) }
    var valueText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(initialMarker.defaultUnit) }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Log hormone reading", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("Today's date: $today", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownOpen,
                onExpandedChange = { dropdownOpen = !dropdownOpen },
            ) {
                OutlinedTextField(
                    value = marker.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hormone") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = dropdownOpen,
                    onDismissRequest = { dropdownOpen = false },
                ) {
                    HormoneMarker.entries.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("${m.displayName} (${m.defaultUnit})") },
                            onClick = {
                                marker = m
                                unit = m.defaultUnit
                                dropdownOpen = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = valueText,
                onValueChange = { v -> valueText = v.filter { it.isDigit() || it == '.' } },
                label = { Text("Value") },
                suffix = { Text(unit) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
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
                    val v = valueText.toFloatOrNull()
                    if (v != null && v >= 0f) {
                        onSave(today, marker, v, unit, notes.ifBlank { null })
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
            Spacer(Modifier.height(8.dp))
        }
    }
}
