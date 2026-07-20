package com.mobile.tamatami.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.training.sections.LogWorkoutSheet
import com.mobile.tamatami.ui.screens.training.sections.RecentWorkoutsList
import com.mobile.tamatami.ui.screens.training.sections.RecommendationCard

@Composable
fun TrainingScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModel.Factory(
            cycleRepository = container.cycleRepository,
            dailyRepository = container.dailyLogRepository,
            workoutRepository = container.workoutRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }
    val today = container.clock.today()

    TamatamiScaffold(
        title = "Training",
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { RecommendationCard(state.recommendation) }
            item {
                Button(
                    onClick = { sheetOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Log workout") }
            }
            if (state.energy != null) {
                item {
                    Text(
                        text = "Recommendation adjusted to today's energy: ${state.energy}/5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item { RecentWorkoutsList(state.recentWorkouts) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (sheetOpen) {
        LogWorkoutSheet(
            date = today,
            onDismiss = { sheetOpen = false },
            onSave = viewModel::logWorkout,
        )
    }
}
