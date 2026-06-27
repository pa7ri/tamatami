package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    ): CycleSnapshot {
        val cycleLen = avgCycleLength.coerceIn(MIN_CYCLE, MAX_CYCLE)
        val periodLen = avgPeriodLength.coerceIn(MIN_PERIOD, MAX_PERIOD)

        if (today.isBefore(lastPeriodStart)) {
            return CycleSnapshot(
                phase = CyclePhase.UNKNOWN,
                cycleDay = 0,
                dayInPhase = 0,
                predictedNextPeriod = lastPeriodStart,
                daysUntilNextPeriod = ChronoUnit.DAYS.between(today, lastPeriodStart).toInt(),
                cycleLength = cycleLen,
            )
        }

        val daysSinceStart = ChronoUnit.DAYS.between(lastPeriodStart, today).toInt()
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

        val nextStart = PeriodPredictor.predict(lastPeriodStart, cycleLen, today)
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
