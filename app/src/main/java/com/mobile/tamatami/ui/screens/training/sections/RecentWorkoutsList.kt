package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.ui.screens.training.displayName
import java.time.format.DateTimeFormatter

@Composable
fun RecentWorkoutsList(workouts: List<WorkoutLogEntity>) {
    Column {
        Text(
            "Recent workouts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        if (workouts.isEmpty()) {
            Text(
                "Nothing logged yet. Log your first session above ↑",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                workouts.forEach { WorkoutRow(it) }
            }
        }
    }
}

@Composable
private fun WorkoutRow(workout: WorkoutLogEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = intensityColor(workout.intensity),
                modifier = Modifier.size(12.dp),
            ) {}
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${workout.type.displayName()} · ${workout.durationMinutes} min",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = workout.date.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!workout.notes.isNullOrBlank()) {
                    Text(
                        text = workout.notes,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = workout.intensity.displayName(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun intensityColor(intensity: WorkoutIntensity): Color = when (intensity) {
    WorkoutIntensity.LOW -> Color(0xFF8FB996)
    WorkoutIntensity.MODERATE -> Color(0xFFFA7E1E)
    WorkoutIntensity.HIGH -> Color(0xFFD62976)
}

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
