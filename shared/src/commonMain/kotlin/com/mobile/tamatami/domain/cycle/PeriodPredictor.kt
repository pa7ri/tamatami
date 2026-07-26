package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.PeriodDay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

data class Prediction(
    val nextDate: LocalDate,
    val daysUntil: Int,
)

/**
 * Predicts the next period start date.
 *
 * - [predict] (static): rolls `lastPeriodStart` forward in `avgCycleLength`-day
 *   steps until the result is after `today`. Used by phase math that doesn't
 *   need history (and by the existing test suite).
 * - [predictAdaptive]: uses [CycleHistory] to derive **both** the anchor (the
 *   most recent detected start, not what the user typed at onboarding) and
 *   the cycle length (rolling average of the last few logged cycles). Falls
 *   back to the profile's onboarding values when history is too thin.
 */
object PeriodPredictor {

    fun predict(
        lastPeriodStart: LocalDate,
        avgCycleLength: Int,
        today: LocalDate,
    ): Prediction {
        val cycleLen = avgCycleLength.coerceAtLeast(1)
        var next = lastPeriodStart.plus(cycleLen, DateTimeUnit.DAY)
        while (next < today) {
            next = next.plus(cycleLen, DateTimeUnit.DAY)
        }
        val daysUntil = today.daysUntil(next)
        return Prediction(next, daysUntil)
    }

    /**
     * History-aware prediction. Picks anchor + cycle length from
     * [loggedPeriodDays] when there's enough data, otherwise falls back to
     * the profile's onboarding values. The returned [Prediction] therefore
     * tracks reality once the user has logged a couple of cycles.
     */
    fun predictAdaptive(
        profile: CycleProfile,
        loggedPeriodDays: List<PeriodDay>,
        today: LocalDate,
    ): Prediction {
        val starts = CycleHistory.detectStarts(loggedPeriodDays)
        val anchor = starts.maxOrNull() ?: profile.lastPeriodStart
        val cycleLen = CycleHistory.avgCycleLength(
            starts = starts,
            fallback = profile.avgCycleLengthDays,
        )
        return predict(anchor, cycleLen, today)
    }
}
