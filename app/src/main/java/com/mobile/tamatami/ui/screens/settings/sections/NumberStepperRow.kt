package com.mobile.tamatami.ui.screens.settings.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Compact +/− stepper for integer settings (cycle length, period length).
 * Buttons clamp at the range bounds so the ViewModel never sees illegal values.
 */
@Composable
fun NumberStepperRow(
    label: String,
    suffix: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        IconButton(
            enabled = value > range.first,
            onClick = { onChange((value - step).coerceIn(range)) },
        ) { Icon(Icons.Outlined.Remove, contentDescription = "Decrease $label") }
        Text(
            "$value $suffix",
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(
            enabled = value < range.last,
            onClick = { onChange((value + step).coerceIn(range)) },
        ) { Icon(Icons.Outlined.Add, contentDescription = "Increase $label") }
    }
}
