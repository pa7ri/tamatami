package com.mobile.tamatami.ui.screens.training

import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.training.TrainingRecommender
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutRecommendation
import com.mobile.tamatami.domain.training.WorkoutType

data class TrainingUiState(
    val recommendation: WorkoutRecommendation,
    val recentWorkouts: List<WorkoutLogEntity>,
    val energy: Int?,
) {
    companion object {
        val Empty = TrainingUiState(
            recommendation = TrainingRecommender.recommend(CyclePhase.UNKNOWN, energy = null),
            recentWorkouts = emptyList(),
            energy = null,
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
}

fun WorkoutIntensity.displayName(): String = when (this) {
    WorkoutIntensity.LOW -> "Low"
    WorkoutIntensity.MODERATE -> "Moderate"
    WorkoutIntensity.HIGH -> "High"
}
