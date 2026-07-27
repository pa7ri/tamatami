package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CyclePhase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class CyclePhaseCalculatorTest {

    private val start = LocalDate(2026, 6, 1)
    private fun LocalDate.plusDays(n: Int) = plus(n, DateTimeUnit.DAY)
    private fun LocalDate.minusDays(n: Int) = minus(n, DateTimeUnit.DAY)

    @Test fun day1IsMenstrual() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start)
        assertEquals(CyclePhase.MENSTRUAL, snap.phase)
        assertEquals(1, snap.cycleDay)
        assertEquals(1, snap.dayInPhase)
    }

    @Test fun lastMenstrualDayEqualsPeriodLength() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(4))
        assertEquals(CyclePhase.MENSTRUAL, snap.phase)
        assertEquals(5, snap.cycleDay)
    }

    @Test fun dayAfterPeriodIsFollicular() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(5))
        assertEquals(CyclePhase.FOLLICULAR, snap.phase)
        assertEquals(6, snap.cycleDay)
    }

    @Test fun midCycleIsOvulatoryFor28DayCycle() {
        // cycleLen=28 -> ovuMid=14, window 13..15
        val snap13 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(12))
        val snap14 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(13))
        val snap15 = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(14))
        assertEquals(CyclePhase.OVULATORY, snap13.phase)
        assertEquals(CyclePhase.OVULATORY, snap14.phase)
        assertEquals(CyclePhase.OVULATORY, snap15.phase)
        assertEquals(14, snap14.cycleDay)
    }

    @Test fun dayAfterOvulatoryWindowIsLuteal() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(15))
        assertEquals(CyclePhase.LUTEAL, snap.phase)
    }

    @Test fun cycle35OvulatoryWindowCentersOnDay17() {
        // cycleLen=35 -> ovuMid=17, window 16..18
        val snap = CyclePhaseCalculator.calculate(start, 35, 5, start.plusDays(16))
        assertEquals(CyclePhase.OVULATORY, snap.phase)
        assertEquals(17, snap.cycleDay)
    }

    @Test fun cycleWrapsToNextCycleDay1Menstrual() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(28))
        assertEquals(CyclePhase.MENSTRUAL, snap.phase)
        assertEquals(1, snap.cycleDay)
    }

    @Test fun todayBeforeLastPeriodStartReturnsUnknown() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.minusDays(3))
        assertEquals(CyclePhase.UNKNOWN, snap.phase)
        assertEquals(0, snap.cycleDay)
        assertEquals(3, snap.daysUntilNextPeriod)
    }

    @Test fun cycleLengthClampedToAllowedRange() {
        val snapShort = CyclePhaseCalculator.calculate(start, 10, 5, start.plusDays(20))
        // 10 clamps to 21
        assertEquals(21, snapShort.cycleLength)
    }

    @Test fun predictedNextPeriodIsLastStartPlusCycleLength() {
        val snap = CyclePhaseCalculator.calculate(start, 28, 5, start.plusDays(10))
        assertEquals(start.plusDays(28), snap.predictedNextPeriod)
        assertEquals(18, snap.daysUntilNextPeriod)
    }
}
