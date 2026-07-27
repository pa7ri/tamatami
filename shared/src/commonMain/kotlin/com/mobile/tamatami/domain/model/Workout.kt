package com.mobile.tamatami.domain.model

import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.datetime.LocalDate

/**
 * A logged workout — the domain-model counterpart of Room's `WorkoutLogEntity`,
 * used by [DailySnapshot] and the UI so those don't depend on the Room type.
 * The data layer maps entity <-> model at the boundary.
 */
data class Workout(
    val id: Long,
    val date: LocalDate,
    val type: WorkoutType,
    val durationMinutes: Int,
    val intensity: WorkoutIntensity,
    val notes: String?,
)
