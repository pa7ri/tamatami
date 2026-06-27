package com.mobile.tamatami.domain.cycle

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.domain.model.CyclePhase
import org.junit.Test
import java.time.LocalDate

class CyclePhaseCalculatorTest {

    private val start = LocalDate.of(2026, 6, 1)

    @Test fun `day 1 is menstrual`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start)
        assertThat(snap.phase).isEqualTo(CyclePhase.MENSTRUAL)
        assertThat(snap.cycleDay).isEqualTo(1)
        assertThat(snap.dayInPhase).isEqualTo(1)
    }

    @Test fun `last menstrual day equals period length`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(4))
        assertThat(snap.phase).isEqualTo(CyclePhase.MENSTRUAL)
        assertThat(snap.cycleDay).isEqualTo(5)
    }

    @Test fun `day after period is follicular`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(5))
        assertThat(snap.phase).isEqualTo(CyclePhase.FOLLICULAR)
        assertThat(snap.cycleDay).isEqualTo(6)
    }

    @Test fun `mid-cycle is ovulatory for 28-day cycle`() {
        // cycleLen=28 -> ovuMid=14, window 13..15
        val snap13 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(12))
        val snap14 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(13))
        val snap15 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(14))
        assertThat(snap13.phase).isEqualTo(CyclePhase.OVULATORY)
        assertThat(snap14.phase).isEqualTo(CyclePhase.OVULATORY)
        assertThat(snap15.phase).isEqualTo(CyclePhase.OVULATORY)
        assertThat(snap14.cycleDay).isEqualTo(14)
    }

    @Test fun `day after ovulatory window is luteal`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(15))
        assertThat(snap.phase).isEqualTo(CyclePhase.LUTEAL)
    }

    @Test fun `35-day cycle ovulatory window centers on day 17 or 18`() {
        // cycleLen=35 -> ovuMid=17, window 16..18
        val snap = CyclePhaseCalculator.calculate(start, 35, 5, start.plusDays(16))
        assertThat(snap.phase).isEqualTo(CyclePhase.OVULATORY)
        assertThat(snap.cycleDay).isEqualTo(17)
    }

    @Test fun `cycle wraps to next cycle day 1 menstrual`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(28))
        assertThat(snap.phase).isEqualTo(CyclePhase.MENSTRUAL)
        assertThat(snap.cycleDay).isEqualTo(1)
    }

    @Test fun `today before lastPeriodStart returns UNKNOWN`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.minusDays(3))
        assertThat(snap.phase).isEqualTo(CyclePhase.UNKNOWN)
        assertThat(snap.cycleDay).isEqualTo(0)
        assertThat(snap.daysUntilNextPeriod).isEqualTo(3)
    }

    @Test fun `cycle length clamped to allowed range`() {
        val snapShort = CyclePhaseCalculator.calculate(start, 10, 5, start.plusDays(20))
        // 10 clamps to 21
        assertThat(snapShort.cycleLength).isEqualTo(21)
    }

    @Test fun `predicted next period is lastStart plus cycleLength`() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(10))
        assertThat(snap.predictedNextPeriod).isEqualTo(start.plusDays(28))
        assertThat(snap.daysUntilNextPeriod).isEqualTo(18)
    }
}
