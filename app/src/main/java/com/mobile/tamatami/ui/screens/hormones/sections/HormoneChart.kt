package com.mobile.tamatami.ui.screens.hormones.sections

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.hormones.HormoneMarker
import java.time.LocalDate

@Composable
fun HormoneChart(
    marker: HormoneMarker,
    points: List<Pair<LocalDate, Float>>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (points.isEmpty()) "No ${marker.displayName} entries in the last 30 days"
                else "Add at least one more ${marker.displayName} entry to see a trend",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val rangeStart = points.first().first
    val rangeEnd = points.last().first
    val dayRange = (java.time.temporal.ChronoUnit.DAYS.between(rangeStart, rangeEnd))
        .coerceAtLeast(1L)
        .toFloat()
    val pointValues = points.map { it.second }
    val expected = marker.expectedRange
    val minY = minOf(pointValues.min(), expected.start)
    val maxY = maxOf(pointValues.max(), expected.endInclusive)
    val yRange = (maxY - minY).coerceAtLeast(0.0001f)

    val primary = MaterialTheme.colorScheme.primary
    val expectedColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        val w = size.width
        val h = size.height

        // Expected-range band.
        val bandTop = h * (1f - (expected.endInclusive - minY) / yRange)
        val bandBottom = h * (1f - (expected.start - minY) / yRange)
        drawRect(
            color = expectedColor,
            topLeft = Offset(0f, bandTop),
            size = androidx.compose.ui.geometry.Size(w, (bandBottom - bandTop).coerceAtLeast(2f)),
        )

        // Line path.
        val path = Path()
        points.forEachIndexed { index, (date, value) ->
            val xRatio = java.time.temporal.ChronoUnit.DAYS.between(rangeStart, date) / dayRange
            val x = xRatio * w
            val y = h * (1f - (value - minY) / yRange)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = primary, style = Stroke(width = 4f))

        // Points.
        points.forEach { (date, value) ->
            val xRatio = java.time.temporal.ChronoUnit.DAYS.between(rangeStart, date) / dayRange
            val x = xRatio * w
            val y = h * (1f - (value - minY) / yRange)
            drawCircle(color = primary, radius = 6f, center = Offset(x, y))
        }
    }
}
