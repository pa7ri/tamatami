package com.mobile.tamatami.domain.cycle

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.domain.model.PeriodFlow
import org.junit.Test
import java.time.LocalDate

class CycleHistoryTest {

    private val d0 = LocalDate.of(2026, 1, 1)

    private fun day(offset: Long, flow: PeriodFlow = PeriodFlow.MEDIUM) =
        PeriodDayEntity(date = d0.plusDays(offset), flow = flow)

    // -------------------------------------------------------------------- detectStarts

    @Test fun `empty input gives no starts`() {
        assertThat(CycleHistory.detectStarts(emptyList())).isEmpty()
    }

    @Test fun `consecutive days collapse to a single start`() {
        // d0 through d0+4 — one bleed.
        val days = (0L..4L).map { day(it) }
        assertThat(CycleHistory.detectStarts(days)).containsExactly(d0)
    }

    @Test fun `gap larger than threshold opens a new start`() {
        // Two bleeds: d0-d0+3 and d0+28-d0+30. Gap of 24 days > default 5.
        val days = (0L..3L).map { day(it) } + (28L..30L).map { day(it) }
        assertThat(CycleHistory.detectStarts(days))
            .containsExactly(d0, d0.plusDays(28)).inOrder()
    }

    @Test fun `unsorted input is sorted before scanning`() {
        // Same as above, shuffled order coming in.
        val days = listOf(day(30), day(0), day(28), day(2), day(3), day(29), day(1))
        assertThat(CycleHistory.detectStarts(days))
            .containsExactly(d0, d0.plusDays(28)).inOrder()
    }

    @Test fun `gap parameter is honored`() {
        // d0 and d0+3 with gapDays = 2 → both are starts.
        val days = listOf(day(0), day(3))
        assertThat(CycleHistory.detectStarts(days, gapDays = 2))
            .containsExactly(d0, d0.plusDays(3)).inOrder()
    }

    // ----------------------------------------------------------------- avgCycleLength

    @Test fun `fewer than two starts falls back`() {
        assertThat(CycleHistory.avgCycleLength(emptyList(), fallback = 28)).isEqualTo(28)
        assertThat(CycleHistory.avgCycleLength(listOf(d0), fallback = 30)).isEqualTo(30)
    }

    @Test fun `averages deltas between consecutive starts`() {
        val starts = listOf(d0, d0.plusDays(28), d0.plusDays(54))  // 28, 26
        assertThat(CycleHistory.avgCycleLength(starts, fallback = 28)).isEqualTo(27)
    }

    @Test fun `window limits how many cycles count`() {
        // Deltas: 30, 30, 30, 25, 25, 25 — full = 27.5 → 28; window=3 = 25.
        val offsets = listOf(0L, 30L, 60L, 90L, 115L, 140L, 165L)
        val starts = offsets.map { d0.plusDays(it) }
        assertThat(CycleHistory.avgCycleLength(starts, fallback = 28, window = 6)).isEqualTo(28)
        assertThat(CycleHistory.avgCycleLength(starts, fallback = 28, window = 3)).isEqualTo(25)
    }

    @Test fun `rounds to nearest int`() {
        // Deltas: 27, 28 → average 27.5 → rounds to 28.
        val starts = listOf(d0, d0.plusDays(27), d0.plusDays(55))
        assertThat(CycleHistory.avgCycleLength(starts, fallback = 0)).isEqualTo(28)
    }
}
