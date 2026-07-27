package com.mobile.tamatami.domain.calendar

import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonthBuilderTest {

    private val start = LocalDate(2026, 6, 1) // Monday
    private fun LocalDate.plusDays(n: Int) = plus(n, DateTimeUnit.DAY)

    private fun profile(
        last: LocalDate = start,
        cycle: Int = 28,
        period: Int = 5,
    ): CycleProfile = CycleProfile(
        lastPeriodStart = last,
        avgCycleLengthDays = cycle,
        avgPeriodLengthDays = period,
    )

    @Test fun gridAlwaysHas42Cells() {
        val days = MonthBuilder.build(2026, 6, profile(), emptyList(), start)
        assertEquals(42, days.size)
    }

    @Test fun june2026HasCorrectLeadingBlanksForMondayStart() {
        // June 1 2026 is a Monday; Sunday-start grid means one leading blank (May 31 = Sun).
        val days = MonthBuilder.build(2026, 6, profile(), emptyList(), start)
        // Index 0 = grid start. June 1 should sit at index 1 (Monday).
        assertEquals(start, days[1].date)
        assertTrue(days[1].inMonth)
        assertFalse(days[0].inMonth) // May 31
    }

    @Test fun loggedPeriodDayFlaggedInReturnedList() {
        val logged = listOf(PeriodDay(date = start.plusDays(1), flow = PeriodFlow.HEAVY))
        val days = MonthBuilder.build(2026, 6, profile(), logged, start)
        val match = days.first { it.date == start.plusDays(1) }
        assertTrue(match.isLoggedPeriod)
        assertEquals(PeriodFlow.HEAVY, match.flow)
    }

    @Test fun predictedNextPeriodDotLandsOnLastStartPlusCycleLength() {
        // last period = May 1, cycle = 28 → next predicted = May 29 (in May view)
        val p = profile(last = LocalDate(2026, 5, 1), cycle = 28)
        val days = MonthBuilder.build(2026, 5, p, emptyList(), LocalDate(2026, 5, 10))
        val predicted = days.first { it.isPredictedPeriod }
        assertEquals(LocalDate(2026, 5, 29), predicted.date)
    }

    @Test fun todayFlagSetOnlyOnTodaysCell() {
        val today = LocalDate(2026, 6, 14)
        val days = MonthBuilder.build(2026, 6, profile(), emptyList(), today)
        assertEquals(1, days.count { it.isToday })
        assertEquals(today, days.first { it.isToday }.date)
    }

    @Test fun phaseTaggingMatchesCyclePhaseCalculatorForEveryInMonthDay() {
        val p = profile()
        val days = MonthBuilder.build(2026, 6, p, emptyList(), start)
        days.filter { it.inMonth }.forEach { day ->
            val expected = CyclePhaseCalculator.calculate(
                lastPeriodStart = p.lastPeriodStart,
                avgCycleLength = p.avgCycleLengthDays,
                avgPeriodLength = p.avgPeriodLengthDays,
                today = day.date,
            ).phase
            assertEquals(expected, day.phase)
        }
    }
}
