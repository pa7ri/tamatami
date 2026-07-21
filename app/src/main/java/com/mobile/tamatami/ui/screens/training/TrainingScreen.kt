package com.mobile.tamatami.ui.screens.training

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.training.sections.LogSleepSheet
import com.mobile.tamatami.ui.screens.training.sections.LogWorkoutSheet
import com.mobile.tamatami.ui.screens.training.sections.RecentWorkoutsList
import com.mobile.tamatami.ui.screens.training.sections.RecommendationCard
import com.mobile.tamatami.ui.screens.training.sections.SleepCard
import com.mobile.tamatami.ui.screens.training.sections.StepsCard

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

@Composable
fun TrainingScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModel.Factory(
            userRepository = container.userRepository,
            cycleRepository = container.cycleRepository,
            dailyRepository = container.dailyLogRepository,
            workoutRepository = container.workoutRepository,
            stepDataSource = container.stepDataSource,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var workoutSheetOpen by remember { mutableStateOf(false) }
    var sleepSheetOpen by remember { mutableStateOf(false) }
    val today = container.clock.today()

    // Health Connect permission grant flow. On return, re-check + fetch steps.
    val permissionLauncher = rememberLauncherForActivityResult(
        viewModel.stepsPermissionContract,
    ) { _ -> viewModel.refreshSteps() }

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
                    onClick = { workoutSheetOpen = true },
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
            item {
                StepsCard(
                    steps = state.steps,
                    goal = state.stepsGoal,
                    onConnect = { permissionLauncher.launch(viewModel.stepsPermissions) },
                    onInstallHealthConnect = { openHealthConnectListing(context) },
                )
            }
            item {
                SleepCard(
                    sleep = state.sleep,
                    goalMinutes = state.sleepGoalMinutes,
                    expected = state.expectedSleep,
                    onLogSleep = { sleepSheetOpen = true },
                )
            }
            item { RecentWorkoutsList(state.recentWorkouts) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (workoutSheetOpen) {
        LogWorkoutSheet(
            date = today,
            onDismiss = { workoutSheetOpen = false },
            onSave = viewModel::logWorkout,
        )
    }

    if (sleepSheetOpen) {
        // Default to a typical 23:00 → 07:00 night, or the existing entry.
        val s = state.sleep
        LogSleepSheet(
            initialBedMinute = s?.bedMinuteOfDay ?: (23 * 60),
            initialWakeMinute = s?.wakeMinuteOfDay ?: (7 * 60),
            initialRating = s?.rating ?: SleepRating.OKAY,
            onDismiss = { sleepSheetOpen = false },
            onSave = { bed, wake, rating -> viewModel.logSleep(bed, wake, rating) },
        )
    }
}

/** Open the Play Store listing for Health Connect (falls back to the web). */
private fun openHealthConnectListing(context: android.content.Context) {
    val uri = Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        val web = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(web) }
    }
}
