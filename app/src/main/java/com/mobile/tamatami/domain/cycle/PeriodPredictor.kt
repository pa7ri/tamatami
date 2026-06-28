package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
        val cycleLen = avgCycleLength.coerceAtLeast(1).toLong()
        var next = lastPeriodStart.plusDays(cycleLen)
        while (next.isBefore(today)) {
            next = next.plusDays(cycleLen)
        }
        val daysUntil = ChronoUnit.DAYS.between(today, next).toInt()
        return Prediction(next, daysUntil)
    }

    /**
     * History-aware prediction. Picks anchor + cycle length from
     * [loggedPeriodDays] when there's enough data, otherwise falls back to
     * the profile's onboarding values. The returned [Prediction] therefore
     * tracks reality once the user has logged a couple of cycles.
     */
    fun predictAdaptive(
        profile: UserProfileEntity,
        loggedPeriodDays: List<PeriodDayEntity>,
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
