package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.PeriodDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Pure phase calculator. Given the user's last period start, their average
 * cycle and period lengths, and a target date, returns the matching
 * [CycleSnapshot].
 *
 * Phase windows (1-indexed cycle day):
 *  - MENSTRUAL  : [1, periodLength]
 *  - FOLLICULAR : (periodLength, cycleLength/2 - 2]
 *  - OVULATORY  : [cycleLength/2 - 1, cycleLength/2 + 1]
 *  - LUTEAL     : (cycleLength/2 + 1, cycleLength]
 *
 * `today < lastPeriodStart` returns a [CyclePhase.UNKNOWN] snapshot so the UI
 * can render an empty state instead of negative-day math.
 *
 * Two entry points:
 *  - [calculate] — pure inputs, used by tests and any caller that already
 *    has the cycle parameters resolved.
 *  - [calculateAdaptive] — passes [loggedPeriodDays] through
 *    [PeriodPredictor.predictAdaptive] so the resulting `predictedNextPeriod`
 *    tracks what's actually been logged.
 */
object CyclePhaseCalculator {

    private const val MIN_CYCLE = 21
    private const val MAX_CYCLE = 45
    private const val MIN_PERIOD = 2
    private const val MAX_PERIOD = 10

    fun calculate(
        lastPeriodStart: LocalDate,
        avgCycleLength: Int,
        avgPeriodLength: Int,
        today: LocalDate,
    ): CycleSnapshot = calculateInternal(
        lastPeriodStart = lastPeriodStart,
        avgCycleLength = avgCycleLength,
        avgPeriodLength = avgPeriodLength,
        today = today,
        prediction = null,
    )

    /**
     * Same shape as [calculate], but derives the anchor + cycle length from
     * [loggedPeriodDays] via [PeriodPredictor.predictAdaptive]. The returned
     * `cycleLength` on the snapshot reflects the *adaptive* length.
     */
    fun calculateAdaptive(
        profile: CycleProfile,
        loggedPeriodDays: List<PeriodDay>,
        today: LocalDate,
    ): CycleSnapshot {
        val adaptive = PeriodPredictor.predictAdaptive(profile, loggedPeriodDays, today)
        val starts = CycleHistory.detectStarts(loggedPeriodDays)
        val anchor = starts.maxOrNull() ?: profile.lastPeriodStart
        val adaptiveCycleLen = CycleHistory.avgCycleLength(
            starts = starts,
            fallback = profile.avgCycleLengthDays,
        )
        return calculateInternal(
            lastPeriodStart = anchor,
            avgCycleLength = adaptiveCycleLen,
            avgPeriodLength = profile.avgPeriodLengthDays,
            today = today,
            prediction = adaptive,
        )
    }

    private fun calculateInternal(
        lastPeriodStart: LocalDate,
        avgCycleLength: Int,
        avgPeriodLength: Int,
        today: LocalDate,
        prediction: Prediction?,
    ): CycleSnapshot {
        val cycleLen = avgCycleLength.coerceIn(MIN_CYCLE, MAX_CYCLE)
        val periodLen = avgPeriodLength.coerceIn(MIN_PERIOD, MAX_PERIOD)

        if (today < lastPeriodStart) {
            return CycleSnapshot(
                phase = CyclePhase.UNKNOWN,
                cycleDay = 0,
                dayInPhase = 0,
                predictedNextPeriod = lastPeriodStart,
                daysUntilNextPeriod = today.daysUntil(lastPeriodStart),
                cycleLength = cycleLen,
            )
        }

        val daysSinceStart = lastPeriodStart.daysUntil(today)
        // 1-indexed day inside the current cycle.
        val cycleDay = (daysSinceStart % cycleLen) + 1

        val ovuMid = cycleLen / 2
        val ovuStart = ovuMid - 1
        val ovuEnd = ovuMid + 1

        val phase = when {
            cycleDay <= periodLen -> CyclePhase.MENSTRUAL
            cycleDay < ovuStart -> CyclePhase.FOLLICULAR
            cycleDay in ovuStart..ovuEnd -> CyclePhase.OVULATORY
            else -> CyclePhase.LUTEAL
        }

        val dayInPhase = when (phase) {
            CyclePhase.MENSTRUAL -> cycleDay
            CyclePhase.FOLLICULAR -> cycleDay - periodLen
            CyclePhase.OVULATORY -> cycleDay - ovuStart + 1
            CyclePhase.LUTEAL -> cycleDay - ovuEnd
            CyclePhase.UNKNOWN -> 0
        }

        val nextStart = prediction ?: PeriodPredictor.predict(lastPeriodStart, cycleLen, today)
        return CycleSnapshot(
            phase = phase,
            cycleDay = cycleDay,
            dayInPhase = dayInPhase,
            predictedNextPeriod = nextStart.nextDate,
            daysUntilNextPeriod = nextStart.daysUntil,
            cycleLength = cycleLen,
        )
    }
}
