package com.mobile.tamatami.domain.calendar

import com.mobile.tamatami.domain.cycle.CycleHistory
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.cycle.PeriodPredictor
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class CalendarDay(
    val date: LocalDate,
    val inMonth: Boolean,
    val phase: CyclePhase,
    val isLoggedPeriod: Boolean,
    val flow: PeriodFlow?,
    val isPredictedPeriod: Boolean,
    val isPredictedOvulation: Boolean,
    val isToday: Boolean,
)

/**
 * Pure builder for a 6×7 calendar grid. The returned list always has 42
 * entries — leading and trailing days outside the displayed month keep
 * `inMonth = false` so the grid never reflows.
 *
 * The month is passed as [year] + [monthNumber] (1-12) rather than a
 * `java.time.YearMonth`, which doesn't exist in kotlinx-datetime; callers on
 * the JVM convert at the boundary.
 *
 * The predicted-period and predicted-ovulation rings are driven by
 * [PeriodPredictor.predictAdaptive] — once the user has logged a couple of
 * real cycles, the rings track their actual length, not their onboarding
 * guess.
 */
object MonthBuilder {

    fun build(
        year: Int,
        monthNumber: Int,
        profile: CycleProfile?,
        loggedPeriodDays: List<PeriodDay>,
        today: LocalDate,
    ): List<CalendarDay> {
        val first = LocalDate(year, monthNumber, 1)
        // Sunday-start grid: shift so that Sunday (ISO 7) maps to 0.
        val leading = first.dayOfWeek.isoDayNumber % 7
        val gridStart = first.minus(leading, DateTimeUnit.DAY)

        val loggedByDate = loggedPeriodDays.associateBy { it.date }

        val predicted: LocalDate? = profile?.let {
            PeriodPredictor.predictAdaptive(it, loggedPeriodDays, today).nextDate
        }
        // Ovulation is half-a-cycle before the next predicted period, using
        // the **adaptive** cycle length rather than the onboarding default.
        val adaptiveCycleLen: Int = profile?.let {
            CycleHistory.avgCycleLength(
                starts = CycleHistory.detectStarts(loggedPeriodDays),
                fallback = it.avgCycleLengthDays,
            )
        } ?: 28
        val predictedOvulation: LocalDate? = predicted?.minus(adaptiveCycleLen / 2, DateTimeUnit.DAY)

        return List(42) { index ->
            val date = gridStart.plus(index, DateTimeUnit.DAY)
            val inMonth = date.year == year && date.monthNumber == monthNumber
            val phase = if (profile != null) {
                CyclePhaseCalculator.calculateAdaptive(
                    profile = profile,
                    loggedPeriodDays = loggedPeriodDays,
                    today = date,
                ).phase
            } else {
                CyclePhase.UNKNOWN
            }
            val logged = loggedByDate[date]
            CalendarDay(
                date = date,
                inMonth = inMonth,
                phase = phase,
                isLoggedPeriod = logged != null,
                flow = logged?.flow,
                isPredictedPeriod = predicted == date,
                isPredictedOvulation = predictedOvulation == date,
                isToday = date == today,
            )
        }
    }

    /** Offset (in days) of the first Sunday on or before [first]. */
    @Suppress("unused") // referenced by tests
    fun leadingBlanks(first: LocalDate): Int = first.dayOfWeek.isoDayNumber % 7
}
