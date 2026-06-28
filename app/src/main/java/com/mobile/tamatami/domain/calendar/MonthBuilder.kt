package com.mobile.tamatami.domain.calendar

import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.domain.cycle.CycleHistory
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.cycle.PeriodPredictor
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.PeriodFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

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
 * entries — leading and trailing days outside [displayedMonth] keep
 * `inMonth = false` so the grid never reflows.
 *
 * The predicted-period and predicted-ovulation rings are driven by
 * [PeriodPredictor.predictAdaptive] — once the user has logged a couple of
 * real cycles, the rings track their actual length, not their onboarding
 * guess.
 */
object MonthBuilder {

    fun build(
        displayedMonth: YearMonth,
        profile: UserProfileEntity?,
        loggedPeriodDays: List<PeriodDayEntity>,
        today: LocalDate,
    ): List<CalendarDay> {
        val first = displayedMonth.atDay(1)
        // Sunday-start grid: shift so that DayOfWeek SUNDAY (7) maps to 0.
        val leading = (first.dayOfWeek.value % 7)
        val gridStart = first.minusDays(leading.toLong())

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
        val predictedOvulation: LocalDate? = predicted?.minusDays(adaptiveCycleLen / 2L)

        return List(42) { index ->
            val date = gridStart.plusDays(index.toLong())
            val inMonth = YearMonth.from(date) == displayedMonth
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
    fun leadingBlanks(first: LocalDate): Int = first.dayOfWeek.value % 7

    @Suppress("unused")
    private val _orderHint = DayOfWeek.SUNDAY // keep import explicit
}
