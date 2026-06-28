package com.mobile.tamatami.ui.screens.calendar.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.calendar.CalendarDay
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.ui.screens.home.displayName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

/**
 * The status card that lives under the month grid. Renders **the selected
 * day's full state** and lets the user edit every field inline — flow, mood,
 * symptoms, water, cravings, training. Replaces the old [DaySheet] modal:
 * selecting a day no longer covers the calendar with a sheet.
 *
 * The card binds [DailySnapshot] from `DailyLogRepository.observeToday` so
 * writes round-trip live.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DayStatusCard(
    day: CalendarDay,
    snapshot: DailySnapshot,
    cycle: CycleSnapshot,
    onFlow: (LocalDate, PeriodFlow) -> Unit,
    onMood: (LocalDate, Mood) -> Unit,
    onEnergy: (LocalDate, Int) -> Unit,
    onToggleSymptom: (LocalDate, Symptom) -> Unit,
    onWater: (LocalDate, Int) -> Unit,
    onCraving: (LocalDate, CravingHint?) -> Unit,
    onAddSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ----- Header --------------------------------------------------
            Text(
                text = day.date.format(dateFmt),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = buildString {
                    append(day.phase.displayName())
                    if (cycle.cycleDay > 0 && day.isToday) {
                        append(" · Day ${cycle.cycleDay} of ${cycle.cycleLength}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            Section("Period flow")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodFlow.entries.forEach { flow ->
                    FilterChip(
                        selected = flow == (snapshot.periodFlow ?: PeriodFlow.NONE),
                        onClick = { onFlow(day.date, flow) },
                        label = { Text(flow.label()) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Section("Mood")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mood.entries.forEach { mood ->
                    FilterChip(
                        selected = mood == snapshot.mood,
                        onClick = { onMood(day.date, mood) },
                        label = { Text(mood.label()) },
                    )
                }
            }
            if (snapshot.mood != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Energy: ${snapshot.energy ?: 3} / 5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = (snapshot.energy ?: 3).toFloat(),
                    onValueChange = { onEnergy(day.date, it.toInt().coerceIn(1, 5)) },
                    valueRange = 1f..5f,
                    steps = 3,
                )
            }

            Spacer(Modifier.height(16.dp))
            Section("Symptoms")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Symptom.entries.forEach { s ->
                    FilterChip(
                        selected = s in snapshot.symptoms,
                        onClick = { onToggleSymptom(day.date, s) },
                        label = { Text(s.label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Section("Water")
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = {
                        onWater(day.date, (snapshot.waterGlasses - 1).coerceAtLeast(0))
                    },
                ) { Icon(Icons.Outlined.Remove, contentDescription = "Remove a glass") }
                Spacer(Modifier.width(16.dp))
                Text(
                    "${snapshot.waterGlasses} / ${snapshot.waterGoal} glasses",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(16.dp))
                FilledIconButton(
                    onClick = { onWater(day.date, snapshot.waterGlasses + 1) },
                ) { Icon(Icons.Outlined.Add, contentDescription = "Add a glass") }
            }

            Spacer(Modifier.height(16.dp))
            Section("Craving")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CravingHint.entries.forEach { c ->
                    FilterChip(
                        selected = c == snapshot.craving,
                        onClick = {
                            // Re-tap clears.
                            onCraving(day.date, if (c == snapshot.craving) null else c)
                        },
                        label = { Text(c.label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Section("Training")
            if (snapshot.workouts.isEmpty()) {
                Text(
                    "No sessions logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    snapshot.workouts.forEach { WorkoutRow(it) }
                }
            }
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = onAddSession,
                label = { Text("Add session in Training") },
            )
        }
    }
}

@Composable
private fun Section(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun WorkoutRow(w: WorkoutLogEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp),
        )
        Text(
            text = "${w.type.label()} · ${w.durationMinutes} min · ${w.intensity.label()}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// --------------------------------------------------------- enum → human label

private fun PeriodFlow.label(): String = when (this) {
    PeriodFlow.NONE -> "None"
    PeriodFlow.SPOTTING -> "Spotting"
    PeriodFlow.LIGHT -> "Light"
    PeriodFlow.MEDIUM -> "Medium"
    PeriodFlow.HEAVY -> "Heavy"
}

private fun Mood.label(): String = when (this) {
    Mood.GREAT -> "Great"
    Mood.GOOD -> "Good"
    Mood.NEUTRAL -> "Neutral"
    Mood.LOW -> "Low"
    Mood.AWFUL -> "Awful"
}

private fun com.mobile.tamatami.domain.training.WorkoutType.label(): String = when (this) {
    com.mobile.tamatami.domain.training.WorkoutType.REST -> "Rest"
    com.mobile.tamatami.domain.training.WorkoutType.YOGA -> "Yoga"
    com.mobile.tamatami.domain.training.WorkoutType.WALK -> "Walk"
    com.mobile.tamatami.domain.training.WorkoutType.STRENGTH -> "Strength"
    com.mobile.tamatami.domain.training.WorkoutType.CARDIO -> "Cardio"
    com.mobile.tamatami.domain.training.WorkoutType.HIIT -> "HIIT"
    com.mobile.tamatami.domain.training.WorkoutType.OTHER -> "Other"
}

private fun com.mobile.tamatami.domain.training.WorkoutIntensity.label(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

@Suppress("unused")
private val _shapeHint = RoundedCornerShape(0.dp) // keep import explicit
