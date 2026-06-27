package com.mobile.tamatami.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.calendar.sections.DaySheet
import com.mobile.tamatami.ui.screens.calendar.sections.MonthGrid
import com.mobile.tamatami.ui.screens.calendar.sections.MonthHeader
import com.mobile.tamatami.ui.theme.PhaseMenstrual
import com.mobile.tamatami.ui.theme.PhaseOvulatory

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
    val selected = state.selectedDate?.let { sel -> state.days.firstOrNull { it.date == sel } }

    TamatamiScaffold(
        title = "Calendar",
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
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
            Legend()
            Spacer(Modifier.height(16.dp))
        }
    }

    if (selected != null) {
        DaySheet(
            day = selected,
            onDismiss = { viewModel.selectDate(null) },
            onFlow = viewModel::setFlow,
            onMood = viewModel::setMood,
        )
    }
}

@Composable
private fun Legend() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Legend", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LegendRow("Logged period day", PhaseMenstrual)
            LegendRow("Predicted next period", PhaseMenstrual, hollow = true)
            LegendRow("Predicted ovulation", PhaseOvulatory, hollow = true)
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    hollow: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (hollow) androidx.compose.ui.graphics.Color.Transparent else color),
        ) {
            if (hollow) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.Transparent)
                        .padding(2.dp),
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
