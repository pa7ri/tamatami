package com.mobile.tamatami.ui.screens.training.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.training.WorkoutRecommendation
import com.mobile.tamatami.ui.screens.training.displayName
import com.mobile.tamatami.ui.theme.phaseBrush

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RecommendationCard(recommendation: WorkoutRecommendation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(phaseBrush(recommendation.phase))
            .padding(24.dp),
    ) {
        Text(
            text = "Today: ${recommendation.headline}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${recommendation.intensity.displayName()} · " +
                "${recommendation.durationMinRange.first}–${recommendation.durationMinRange.last} min",
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recommendation.suggestedTypes.forEach { type ->
                Surface(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = type.displayName(),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = recommendation.rationale,
            color = Color.White.copy(alpha = 0.95f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
