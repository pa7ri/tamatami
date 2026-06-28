package com.mobile.tamatami.domain.model

import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.nutrition.CravingHint
import java.time.LocalDate

/** Everything the user has logged for a single date. */
data class DailySnapshot(
    val date: LocalDate,
    val waterGlasses: Int,
    val waterGoal: Int,
    val mood: Mood?,
    val energy: Int?,
    val moodNotes: String?,
    val periodFlow: PeriodFlow?,
    val symptoms: Set<Symptom>,
    val craving: CravingHint?,
    val workouts: List<WorkoutLogEntity>,
) {
    companion object {
        fun empty(date: LocalDate): DailySnapshot = DailySnapshot(
            date = date,
            waterGlasses = 0,
            waterGoal = 8,
            mood = null,
            energy = null,
            moodNotes = null,
            periodFlow = null,
            symptoms = emptySet(),
            craving = null,
            workouts = emptyList(),
        )
    }
}
