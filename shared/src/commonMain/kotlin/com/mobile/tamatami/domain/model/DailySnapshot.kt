package com.mobile.tamatami.domain.model

import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.sleep.SleepQuality
import com.mobile.tamatami.domain.sleep.SleepRating
import kotlinx.datetime.LocalDate

/** Last night's sleep as shown on a day — duration, self-rating, and the estimated quality. */
data class SleepSummary(
    val bedMinuteOfDay: Int,
    val wakeMinuteOfDay: Int,
    val durationMinutes: Int,
    val rating: SleepRating,
    val quality: SleepQuality,
)

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
    val workouts: List<Workout>,
    val sleep: SleepSummary?,
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
            sleep = null,
        )
    }
}
