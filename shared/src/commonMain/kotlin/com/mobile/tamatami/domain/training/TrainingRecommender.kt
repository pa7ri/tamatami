package com.mobile.tamatami.domain.training

import com.mobile.tamatami.domain.model.CyclePhase

/**
 * Maps a cycle phase plus today's reported energy onto a concrete workout
 * recommendation. Pure function — easy to test, easy to tweak.
 *
 * Per-phase defaults (energy = null):
 *   MENSTRUAL  → REST/YOGA/WALK · LOW      · 20..30 min
 *   FOLLICULAR → STRENGTH/CARDIO · MODERATE · 30..45 min
 *   OVULATORY  → STRENGTH/HIIT   · HIGH    · 30..60 min
 *   LUTEAL     → STRENGTH/CARDIO · MODERATE · 30..45 min
 *   UNKNOWN    → WALK/YOGA       · LOW     · 20..30 min   (safe fallback)
 *
 * Energy overrides:
 *   energy <= 2 → downshift intensity one step and swap types
 *                 (HIIT → CARDIO; STRENGTH → YOGA/WALK).
 *   energy == 5 && phase == LUTEAL → allow stepping intensity up to HIGH.
 *   null energy → leave defaults alone.
 */
object TrainingRecommender {

    fun recommend(phase: CyclePhase, energy: Int?): WorkoutRecommendation {
        val base = baseFor(phase)
        val needsDownshift = energy != null && energy <= 2
        val canStepUp = energy == 5 && phase == CyclePhase.LUTEAL

        var intensity = base.intensity
        var types = base.suggestedTypes
        var headline = base.headline
        var rationale = base.rationale

        if (needsDownshift) {
            intensity = intensity.downshift()
            types = types.map { swapForLowEnergy(it) }.distinct()
            headline = lowEnergyHeadline(phase)
            rationale = "$rationale Today your reported energy is low, so we've eased it back."
        } else if (canStepUp) {
            intensity = intensity.upshift()
            headline = "Strong luteal day"
            rationale = "$rationale Your energy is high — you can lean into a heavier session."
        }

        return base.copy(
            suggestedTypes = types,
            intensity = intensity,
            headline = headline,
            rationale = rationale,
        )
    }

    private fun baseFor(phase: CyclePhase): WorkoutRecommendation = when (phase) {
        CyclePhase.MENSTRUAL -> WorkoutRecommendation(
            phase = phase,
            suggestedTypes = listOf(WorkoutType.REST, WorkoutType.YOGA, WorkoutType.WALK),
            intensity = WorkoutIntensity.LOW,
            durationMinRange = 20..30,
            headline = "Restorative day",
            rationale = "Energy tends to dip during the first few days of your period. " +
                "Gentle movement supports circulation without taxing recovery.",
        )
        CyclePhase.FOLLICULAR -> WorkoutRecommendation(
            phase = phase,
            suggestedTypes = listOf(WorkoutType.STRENGTH, WorkoutType.CARDIO),
            intensity = WorkoutIntensity.MODERATE,
            durationMinRange = 30..45,
            headline = "Build phase",
            rationale = "Rising estrogen makes this a great window for new programs, " +
                "skill work, and progressive strength.",
        )
        CyclePhase.OVULATORY -> WorkoutRecommendation(
            phase = phase,
            suggestedTypes = listOf(WorkoutType.STRENGTH, WorkoutType.HIIT),
            intensity = WorkoutIntensity.HIGH,
            durationMinRange = 30..60,
            headline = "Peak day",
            rationale = "Your body is primed for maximal effort — heavy lifts, sprints, " +
                "and PR attempts often land best here.",
        )
        CyclePhase.LUTEAL -> WorkoutRecommendation(
            phase = phase,
            suggestedTypes = listOf(WorkoutType.STRENGTH, WorkoutType.CARDIO),
            intensity = WorkoutIntensity.MODERATE,
            durationMinRange = 30..45,
            headline = "Steady miles",
            rationale = "Progesterone favours steady-state work and moderate strength. " +
                "Taper as PMS approaches.",
        )
        CyclePhase.UNKNOWN -> WorkoutRecommendation(
            phase = phase,
            suggestedTypes = listOf(WorkoutType.WALK, WorkoutType.YOGA),
            intensity = WorkoutIntensity.LOW,
            durationMinRange = 20..30,
            headline = "Move gently",
            rationale = "Log your last period to unlock phase-aware recommendations.",
        )
    }

    private fun swapForLowEnergy(type: WorkoutType): WorkoutType = when (type) {
        WorkoutType.HIIT -> WorkoutType.CARDIO
        WorkoutType.STRENGTH -> WorkoutType.YOGA
        WorkoutType.CARDIO -> WorkoutType.WALK
        else -> type
    }

    private fun lowEnergyHeadline(phase: CyclePhase): String = when (phase) {
        CyclePhase.OVULATORY -> "Pull back today"
        CyclePhase.FOLLICULAR -> "Easier follicular day"
        CyclePhase.LUTEAL -> "Gentle luteal day"
        CyclePhase.MENSTRUAL -> "Rest is the work"
        CyclePhase.UNKNOWN -> "Move gently"
    }
}
