package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CycleHistoryTest {

    private val d0 = LocalDate(2026, 1, 1)
    private fun LocalDate.plusDays(n: Int) = plus(n, DateTimeUnit.DAY)

    private fun day(offset: Int, flow: PeriodFlow = PeriodFlow.MEDIUM) =
        PeriodDay(date = d0.plusDays(offset), flow = flow)

    // -------------------------------------------------------------------- detectStarts

    @Test fun emptyInputGivesNoStarts() {
        assertTrue(CycleHistory.detectStarts(emptyList()).isEmpty())
    }

    @Test fun consecutiveDaysCollapseToASingleStart() {
        // d0 through d0+4 — one bleed.
        val days = (0..4).map { day(it) }
        assertEquals(listOf(d0), CycleHistory.detectStarts(days))
    }

    @Test fun gapLargerThanThresholdOpensANewStart() {
        // Two bleeds: d0-d0+3 and d0+28-d0+30. Gap of 24 days > default 5.
        val days = (0..3).map { day(it) } + (28..30).map { day(it) }
        assertEquals(listOf(d0, d0.plusDays(28)), CycleHistory.detectStarts(days))
    }

    @Test fun unsortedInputIsSortedBeforeScanning() {
        // Same as above, shuffled order coming in.
        val days = listOf(day(30), day(0), day(28), day(2), day(3), day(29), day(1))
        assertEquals(listOf(d0, d0.plusDays(28)), CycleHistory.detectStarts(days))
    }

    @Test fun gapParameterIsHonored() {
        // d0 and d0+3 with gapDays = 2 → both are starts.
        val days = listOf(day(0), day(3))
        assertEquals(listOf(d0, d0.plusDays(3)), CycleHistory.detectStarts(days, gapDays = 2))
    }

    // ----------------------------------------------------------------- avgCycleLength

    @Test fun fewerThanTwoStartsFallsBack() {
        assertEquals(28, CycleHistory.avgCycleLength(emptyList(), fallback = 28))
        assertEquals(30, CycleHistory.avgCycleLength(listOf(d0), fallback = 30))
    }

    @Test fun averagesDeltasBetweenConsecutiveStarts() {
        val starts = listOf(d0, d0.plusDays(28), d0.plusDays(54))  // 28, 26
        assertEquals(27, CycleHistory.avgCycleLength(starts, fallback = 28))
    }

    @Test fun windowLimitsHowManyCyclesCount() {
        // Deltas: 30, 30, 30, 25, 25, 25 — full = 27.5 → 28; window=3 = 25.
        val offsets = listOf(0, 30, 60, 90, 115, 140, 165)
        val starts = offsets.map { d0.plusDays(it) }
        assertEquals(28, CycleHistory.avgCycleLength(starts, fallback = 28, window = 6))
        assertEquals(25, CycleHistory.avgCycleLength(starts, fallback = 28, window = 3))
    }

    @Test fun roundsToNearestInt() {
        // Deltas: 27, 28 → average 27.5 → rounds to 28.
        val starts = listOf(d0, d0.plusDays(27), d0.plusDays(55))
        assertEquals(28, CycleHistory.avgCycleLength(starts, fallback = 0))
    }
}
