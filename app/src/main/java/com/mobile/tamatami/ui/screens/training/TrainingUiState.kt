package com.mobile.tamatami.ui.screens.training

import com.mobile.tamatami.data.health.StepDataSource
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.SleepSummary
import com.mobile.tamatami.domain.model.Workout
import com.mobile.tamatami.domain.sleep.ExpectedSleepPredictor
import com.mobile.tamatami.domain.training.TrainingRecommender
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutRecommendation
import com.mobile.tamatami.domain.training.WorkoutType

/** What the Steps card should render, driven by Health Connect availability + permission. */
sealed interface StepsUiState {
    /** Health Connect app missing or needs an update — prompt to install. */
    data class Unavailable(val reason: StepDataSource.Availability) : StepsUiState
    /** HC present but read-steps not yet granted — prompt to connect. */
    data object NeedsPermission : StepsUiState
    /** Granted; [count] is today's total (null while first loading). */
    data class Ready(val count: Long?) : StepsUiState
    /** Not yet checked. */
    data object Loading : StepsUiState
}

data class TrainingUiState(
    val recommendation: WorkoutRecommendation,
    val recentWorkouts: List<Workout>,
    val energy: Int?,
    val sleep: SleepSummary?,
    val steps: StepsUiState,
    val stepsGoal: Int,
    val sleepGoalMinutes: Int,
    val expectedSleep: ExpectedSleepPredictor.Prediction?,
) {
    companion object {
        val Empty = TrainingUiState(
            recommendation = TrainingRecommender.recommend(CyclePhase.UNKNOWN, energy = null),
            recentWorkouts = emptyList(),
            energy = null,
            sleep = null,
            steps = StepsUiState.Loading,
            stepsGoal = 8_000,
            sleepGoalMinutes = 8 * 60,
            expectedSleep = null,
        )
    }
}

fun WorkoutType.displayName(): String = when (this) {
    WorkoutType.REST -> "Rest"
    WorkoutType.YOGA -> "Yoga"
    WorkoutType.WALK -> "Walk"
    WorkoutType.STRENGTH -> "Strength"
    WorkoutType.CARDIO -> "Cardio"
    WorkoutType.HIIT -> "HIIT"
    WorkoutType.OTHER -> "Other"
    else -> name.lowercase().replaceFirstChar { it.uppercase() }
}

fun WorkoutIntensity.displayName(): String = when (this) {
    WorkoutIntensity.LOW -> "Low"
    WorkoutIntensity.MODERATE -> "Moderate"
    WorkoutIntensity.HIGH -> "High"
}
