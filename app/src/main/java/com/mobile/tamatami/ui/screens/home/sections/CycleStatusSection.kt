package com.mobile.tamatami.ui.screens.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.ui.screens.home.HomeUiState
import com.mobile.tamatami.ui.screens.home.displayName
import com.mobile.tamatami.ui.theme.LocalPhaseColors

@Composable
fun CycleStatusSection(state: HomeUiState) {
    val phaseColors = LocalPhaseColors.current
    val current = state.cycle.phase
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (current == CyclePhase.UNKNOWN) "Cycle data pending"
                else "Day ${state.cycle.cycleDay} of ${state.cycle.cycleLength} · ${current.displayName()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    CyclePhase.MENSTRUAL,
                    CyclePhase.FOLLICULAR,
                    CyclePhase.OVULATORY,
                    CyclePhase.LUTEAL,
                ).forEachIndexed { index, phase ->
                    if (index > 0) Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                color = if (phase == current) phaseColors.colorFor(phase)
                                else phaseColors.colorFor(phase).copy(alpha = 0.25f),
                            ),
                    )
                }
            }
        }
    }
}
