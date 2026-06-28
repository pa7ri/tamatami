package com.mobile.tamatami.ui.screens.settings.sections

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Material 3 date picker hosted in a dialog, plus the small boilerplate needed
 * to convert between [LocalDate] and the picker's UTC-millis API. Kept here so
 * the calling screen stays readable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogCompat(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    // The picker is calibrated in UTC milliseconds since epoch — pass the
    // start-of-day so the highlighted cell matches `initial`.
    val initialMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val ms = pickerState.selectedDateMillis ?: initialMillis
                val picked = java.time.Instant.ofEpochMilli(ms)
                    .atOffset(ZoneOffset.UTC).toLocalDate()
                onConfirm(picked)
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = pickerState)
    }
}
