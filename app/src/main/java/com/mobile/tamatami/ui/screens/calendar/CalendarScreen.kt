package com.mobile.tamatami.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.nav.TamatamiRoute
import com.mobile.tamatami.ui.screens.calendar.sections.DayStatusCard
import com.mobile.tamatami.ui.screens.calendar.sections.MonthGrid
import com.mobile.tamatami.ui.screens.calendar.sections.MonthHeader
import com.mobile.tamatami.ui.theme.PhaseFollicular
import com.mobile.tamatami.ui.theme.PhaseLuteal
import com.mobile.tamatami.ui.theme.PhaseMenstrual
import com.mobile.tamatami.ui.theme.PhaseOvulatory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.Factory(
            userRepository = container.userRepository,
            cycleRepository = container.cycleRepository,
            dailyLogRepository = container.dailyLogRepository,
            periodDayDao = container.periodDayDao,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    // Day card defaults to today when nothing is explicitly selected — the
    // space under the grid is never empty.
    val displayedDay = state.selectedDate?.let { sel -> state.days.firstOrNull { it.date == sel } }
        ?: state.days.firstOrNull { it.isToday }

    var showLegend by remember { mutableStateOf(false) }

    TamatamiScaffold(
        title = "Calendar",
        bottomBar = { TamatamiBottomBar(navController) },
        actions = {
            IconButton(onClick = { showLegend = true }) {
                Icon(Icons.Outlined.Info, contentDescription = "Calendar legend")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            MonthHeader(
                displayedMonth = state.displayedMonth,
                onPrev = viewModel::goPrevMonth,
                onNext = viewModel::goNextMonth,
            )
            MonthGrid(
                days = state.days,
                onDayClick = { viewModel.selectDate(it.date) },
            )
            Spacer(Modifier.height(16.dp))
            if (displayedDay != null && state.selectedDaySnapshot != null) {
                DayStatusCard(
                    day = displayedDay,
                    snapshot = state.selectedDaySnapshot!!,
                    cycle = state.cycle,
                    onFlow = viewModel::setFlow,
                    onMood = viewModel::setMood,
                    onEnergy = viewModel::setEnergy,
                    onToggleSymptom = viewModel::toggleSymptom,
                    onWater = viewModel::setWater,
                    onCraving = viewModel::setCraving,
                    onAddSession = { navController.navigate(TamatamiRoute.Training) },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLegend) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLegend = false },
            sheetState = sheetState,
        ) {
            Legend()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Legend() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Legend", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "How to read each day on the calendar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Phase tint (fill color of the day)")
            LegendRow(
                title = "Menstrual",
                description = "Bleeding phase — typically the first days of your cycle.",
                swatch = { FillSwatch(PhaseMenstrual) },
            )
            LegendRow(
                title = "Follicular",
                description = "After your period, before ovulation — estrogen rising.",
                swatch = { FillSwatch(PhaseFollicular) },
            )
            LegendRow(
                title = "Ovulatory",
                description = "Fertile window around ovulation.",
                swatch = { FillSwatch(PhaseOvulatory) },
            )
            LegendRow(
                title = "Luteal",
                description = "Post‑ovulation, leading up to your next period.",
                swatch = { FillSwatch(PhaseLuteal) },
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Ring (outline around the day)")
            LegendRow(
                title = "Today",
                description = "A blue ring marks the current date.",
                swatch = { RingSwatch(MaterialTheme.colorScheme.primary) },
            )
            LegendRow(
                title = "Predicted next period",
                description = "Pink ring on days we expect your next period to start.",
                swatch = { RingSwatch(PhaseMenstrual) },
            )
            LegendRow(
                title = "Predicted ovulation",
                description = "Orange ring on the predicted ovulation day.",
                swatch = { RingSwatch(PhaseOvulatory) },
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Inner dot (logged period day)")
            LegendRow(
                title = "Logged period — spotting",
                description = "Lightest dot. Tap a day to log flow.",
                swatch = { DotSwatch(0.30f) },
            )
            LegendRow(
                title = "Logged period — light",
                description = "Light flow.",
                swatch = { DotSwatch(0.55f) },
            )
            LegendRow(
                title = "Logged period — medium",
                description = "Medium flow.",
                swatch = { DotSwatch(0.80f) },
            )
            LegendRow(
                title = "Logged period — heavy",
                description = "Heaviest flow. The darker the dot, the heavier the flow.",
                swatch = { DotSwatch(1.0f) },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}

@Composable
private fun LegendRow(
    title: String,
    description: String,
    swatch: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
            swatch()
        }
        Spacer(Modifier.size(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Solid filled circle — matches a day's phase tint background. */
@Composable
private fun FillSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f)),
    )
}

/** Hollow ring — matches the border drawn around special days. */
@Composable
private fun RingSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .border(width = 2.dp, color = color, shape = CircleShape),
    )
}

/** Small inner dot — matches the logged-period mark; alpha encodes flow intensity. */
@Composable
private fun DotSwatch(alpha: Float) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(PhaseMenstrual.copy(alpha = alpha)),
        )
    }
}
