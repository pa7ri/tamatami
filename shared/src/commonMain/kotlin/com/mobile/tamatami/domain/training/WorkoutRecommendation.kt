package com.mobile.tamatami.domain.training

import com.mobile.tamatami.domain.model.CyclePhase

data class WorkoutRecommendation(
    val phase: CyclePhase,
    val suggestedTypes: List<WorkoutType>,
    val intensity: WorkoutIntensity,
    val durationMinRange: IntRange,
    val headline: String,
    val rationale: String,
)
