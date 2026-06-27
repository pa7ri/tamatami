package com.mobile.tamatami.domain.calendar

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.model.PeriodFlow
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class MonthBuilderTest {

    private val start = LocalDate.of(2026, 6, 1) // Monday

    private fun profile(
        last: LocalDate = start,
        cycle: Int = 28,
        period: Int = 5,
    ): UserProfileEntity = UserProfileEntity(
        id = 0,
        tamaName = "Tama",
        lastPeriodStart = last,
        avgCycleLengthDays = cycle,
        avgPeriodLengthDays = period,
        tryingToConceive = false,
        onContraception = false,
        irregularCycles = false,
        onboardingComplete = true,
        createdAt = Instant.parse("2026-06-01T00:00:00Z"),
    )

    @Test fun `grid always has 42 cells`() {
        val days = MonthBuilder.build(YearMonth.of(2026, 6), profile(), emptyList(), start)
        assertThat(days).hasSize(42)
    }

    @Test fun `june 2026 has correct leading blanks for monday start`() {
        // June 1 2026 is a Monday; Sunday-start grid means one leading blank (May 31, Sun? actually May 31 = Sun).
        val days = MonthBuilder.build(YearMonth.of(2026, 6), profile(), emptyList(), start)
        // Index 0 = grid start. June 1 should sit at index 1 (Monday).
        assertThat(days[1].date).isEqualTo(start)
        assertThat(days[1].inMonth).isTrue()
        assertThat(days[0].inMonth).isFalse() // May 31
    }

    @Test fun `logged period day flagged in returned list`() {
        val logged = listOf(PeriodDayEntity(date = start.plusDays(1), flow = PeriodFlow.HEAVY))
        val days = MonthBuilder.build(YearMonth.of(2026, 6), profile(), logged, start)
        val match = days.first { it.date == start.plusDays(1) }
        assertThat(match.isLoggedPeriod).isTrue()
        assertThat(match.flow).isEqualTo(PeriodFlow.HEAVY)
    }

    @Test fun `predicted next period dot lands on lastStart plus cycleLength`() {
        // last period = May 1, cycle = 28 → next predicted = May 29 (in May view)
        val p = profile(last = LocalDate.of(2026, 5, 1), cycle = 28)
        val days = MonthBuilder.build(YearMonth.of(2026, 5), p, emptyList(), LocalDate.of(2026, 5, 10))
        val predicted = days.first { it.isPredictedPeriod }
        assertThat(predicted.date).isEqualTo(LocalDate.of(2026, 5, 29))
    }

    @Test fun `today flag set only on todays cell`() {
        val today = LocalDate.of(2026, 6, 14)
        val days = MonthBuilder.build(YearMonth.of(2026, 6), profile(), emptyList(), today)
        assertThat(days.count { it.isToday }).isEqualTo(1)
        assertThat(days.first { it.isToday }.date).isEqualTo(today)
    }

    @Test fun `phase tagging matches CyclePhaseCalculator for every in-month day`() {
        val p = profile()
        val days = MonthBuilder.build(YearMonth.of(2026, 6), p, emptyList(), start)
        days.filter { it.inMonth }.forEach { day ->
            val expected = CyclePhaseCalculator.calculate(
                lastPeriodStart = p.lastPeriodStart,
                avgCycleLength = p.avgCycleLengthDays,
                avgPeriodLength = p.avgPeriodLengthDays,
                today = day.date,
            ).phase
            assertThat(day.phase).isEqualTo(expected)
        }
    }
}
