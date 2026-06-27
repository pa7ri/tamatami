package com.mobile.tamatami.domain.calendar

import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
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
            PeriodPredictor.predict(it.lastPeriodStart, it.avgCycleLengthDays, today).nextDate
        }
        val predictedOvulation: LocalDate? = predicted?.minusDays(
            (profile?.avgCycleLengthDays ?: 28) / 2L
        )

        return List(42) { index ->
            val date = gridStart.plusDays(index.toLong())
            val inMonth = YearMonth.from(date) == displayedMonth
            val phase = if (profile != null) {
                CyclePhaseCalculator.calculate(
                    lastPeriodStart = profile.lastPeriodStart,
                    avgCycleLength = profile.avgCycleLengthDays,
                    avgPeriodLength = profile.avgPeriodLengthDays,
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
