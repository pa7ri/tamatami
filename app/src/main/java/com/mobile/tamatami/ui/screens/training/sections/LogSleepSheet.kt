package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.sleep.SleepQualityEstimator
import com.mobile.tamatami.domain.sleep.SleepRating

/**
 * Manual sleep entry: bedtime + wake time (each via a Material time picker) and
 * a quick self-rating. `onSave` receives bed/wake as minute-of-day and the
 * rating; the ViewModel computes duration + quality. Mirrors [LogWorkoutSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSleepSheet(
    initialBedMinute: Int,
    initialWakeMinute: Int,
    initialRating: SleepRating,
    onDismiss: () -> Unit,
    onSave: (bedMinuteOfDay: Int, wakeMinuteOfDay: Int, rating: SleepRating) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bedMinute by remember { mutableIntStateOf(initialBedMinute) }
    var wakeMinute by remember { mutableIntStateOf(initialWakeMinute) }
    var rating by remember { mutableStateOf(initialRating) }
    var picking by remember { mutableStateOf<Picking?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Log sleep", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { picking = Picking.BED },
                    modifier = Modifier.weight(1f),
                ) { Text("Bed ${formatClock(bedMinute)}") }
                OutlinedButton(
                    onClick = { picking = Picking.WAKE },
                    modifier = Modifier.weight(1f),
                ) { Text("Wake ${formatClock(wakeMinute)}") }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Duration: ${formatDuration(SleepQualityEstimator.durationMinutes(bedMinute, wakeMinute))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            Text("How did you sleep?", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SleepRating.entries.forEachIndexed { index, r ->
                    SegmentedButton(
                        selected = r == rating,
                        onClick = { rating = r },
                        shape = SegmentedButtonDefaults.itemShape(index, SleepRating.entries.size),
                    ) { Text(r.label()) }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(bedMinute, wakeMinute, rating)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save sleep") }
            Spacer(Modifier.height(8.dp))
        }
    }

    picking?.let { which ->
        val initial = if (which == Picking.BED) bedMinute else wakeMinute
        TimePickerDialog(
            initialMinuteOfDay = initial,
            onDismiss = { picking = null },
            onConfirm = { mins ->
                if (which == Picking.BED) bedMinute = mins else wakeMinute = mins
                picking = null
            },
        )
    }
}

private enum class Picking { BED, WAKE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialMinuteOfDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (minuteOfDay: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialMinuteOfDay / 60,
        initialMinute = initialMinuteOfDay % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = state) },
    )
}

private fun SleepRating.label(): String = when (this) {
    SleepRating.POOR -> "Poor"
    SleepRating.OKAY -> "Okay"
    SleepRating.RESTFUL -> "Restful"
}

private fun formatClock(minuteOfDay: Int): String {
    val h = (minuteOfDay / 60) % 24
    val m = minuteOfDay % 60
    return "%02d:%02d".format(h, m)
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}
