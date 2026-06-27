package com.mobile.tamatami.domain.cycle

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Prediction(
    val nextDate: LocalDate,
    val daysUntil: Int,
)

/**
 * Predicts the next period start date by rolling `lastPeriodStart` forward in
 * `avgCycleLength`-day steps until the result is after `today`. If today is
 * still inside the most recent cycle, returns that next date and `daysUntil`
 * is positive; if today *is* the predicted date, `daysUntil = 0`.
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
}
