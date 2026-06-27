package com.mobile.tamatami.ui.screens.calendar.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.calendar.CalendarDay
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.ui.theme.LocalPhaseColors
import com.mobile.tamatami.ui.theme.PhaseMenstrual
import com.mobile.tamatami.ui.theme.PhaseOvulatory

@Composable
fun MonthGrid(
    days: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WeekdayHeader()
        // Render 6 rows of 7 cells.
        for (rowIndex in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (col in 0 until 7) {
                    val index = rowIndex * 7 + col
                    if (index < days.size) {
                        DayCell(
                            day = days[index],
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(days[index]) },
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.forEach { label ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val phaseColors = LocalPhaseColors.current
    val bg = if (day.inMonth) {
        phaseColors.colorFor(day.phase).copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }
    val borderColor = when {
        day.isToday -> MaterialTheme.colorScheme.primary
        day.isPredictedPeriod -> PhaseMenstrual
        day.isPredictedOvulation -> PhaseOvulatory
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (borderColor == Color.Transparent) 0.dp else 2.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .clickable(enabled = day.inMonth, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (day.inMonth) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            )
            if (day.isLoggedPeriod) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PhaseMenstrual.copy(alpha = flowAlpha(day.flow))),
                )
            }
        }
    }
}

private fun flowAlpha(flow: PeriodFlow?): Float = when (flow) {
    PeriodFlow.SPOTTING -> 0.3f
    PeriodFlow.LIGHT -> 0.55f
    PeriodFlow.MEDIUM -> 0.8f
    PeriodFlow.HEAVY -> 1.0f
    else -> 0.5f
}
