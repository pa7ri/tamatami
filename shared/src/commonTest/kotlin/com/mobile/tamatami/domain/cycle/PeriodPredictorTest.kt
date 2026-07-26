package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class PeriodPredictorTest {

    private val start = LocalDate(2026, 6, 1)
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

    private fun bleed(from: LocalDate, lengthDays: Int) =
        (0 until lengthDays).map { PeriodDay(date = from.plusDays(it), flow = PeriodFlow.MEDIUM) }

    // ---------------------------------------------------------------------- predict

    @Test fun nextPeriodPredictedWithinFirstCycle() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(10))
        assertEquals(start.plusDays(28), p.nextDate)
        assertEquals(18, p.daysUntil)
    }

    @Test fun rollsForwardAcrossMultipleCycles() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(70))
        assertEquals(start.plusDays(84), p.nextDate)
        assertEquals(14, p.daysUntil)
    }

    @Test fun todayEqualsPredictedDateReturnsZeroDays() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(28))
        assertEquals(start.plusDays(28), p.nextDate)
        assertEquals(0, p.daysUntil)
    }

    @Test fun nonDefaultCycleLengthIsHonored() {
        val p = PeriodPredictor.predict(start, 35, start.plusDays(5))
        assertEquals(start.plusDays(35), p.nextDate)
        assertEquals(30, p.daysUntil)
    }

    // -------------------------------------------------------------- predictAdaptive

    @Test fun adaptiveWithNoLogsFallsBackToProfile() {
        val p = PeriodPredictor.predictAdaptive(profile(), emptyList(), start.plusDays(10))
        assertEquals(start.plusDays(28), p.nextDate)
        assertEquals(18, p.daysUntil)
    }

    @Test fun adaptivePicksLatestLoggedStartAsAnchor() {
        // Onboarding said May 1, but we've actually logged a bleed starting June 1.
        val onboardingStart = LocalDate(2026, 5, 1)
        val realStart = LocalDate(2026, 6, 1)
        val days = bleed(realStart, 4)
        val p = PeriodPredictor.predictAdaptive(
            profile = profile(last = onboardingStart, cycle = 28),
            loggedPeriodDays = days,
            today = realStart.plusDays(5),
        )
        // Anchor = realStart, cycle len falls back to 28 (only one logged start).
        assertEquals(realStart.plusDays(28), p.nextDate)
    }

    @Test fun adaptiveCycleLengthComesFromRollingAverageOfLogs() {
        // Two complete cycles: starts 28 days apart, then 26 days apart → avg 27.
        val s0 = LocalDate(2026, 4, 1)
        val s1 = s0.plusDays(28)
        val s2 = s1.plusDays(26)
        val days = bleed(s0, 4) + bleed(s1, 4) + bleed(s2, 3)
        val p = PeriodPredictor.predictAdaptive(
            profile = profile(last = s0, cycle = 28),
            loggedPeriodDays = days,
            today = s2.plusDays(5),
        )
        // Anchor = s2, adaptive cycle len = 27.
        assertEquals(s2.plusDays(27), p.nextDate)
    }
}
