package com.mobile.tamatami.domain.cycle

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.domain.model.PeriodFlow
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PeriodPredictorTest {

    private val start = LocalDate.of(2026, 6, 1)

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

    private fun bleed(from: LocalDate, lengthDays: Int) =
        (0 until lengthDays).map { PeriodDayEntity(date = from.plusDays(it.toLong()), flow = PeriodFlow.MEDIUM) }

    // ---------------------------------------------------------------------- predict

    @Test fun `next period predicted within first cycle`() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(10))
        assertThat(p.nextDate).isEqualTo(start.plusDays(28))
        assertThat(p.daysUntil).isEqualTo(18)
    }

    @Test fun `rolls forward across multiple cycles`() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(70))
        assertThat(p.nextDate).isEqualTo(start.plusDays(84))
        assertThat(p.daysUntil).isEqualTo(14)
    }

    @Test fun `today equals predicted date returns zero days`() {
        val p = PeriodPredictor.predict(start, 28, start.plusDays(28))
        assertThat(p.nextDate).isEqualTo(start.plusDays(28))
        assertThat(p.daysUntil).isEqualTo(0)
    }

    @Test fun `non-default cycle length is honored`() {
        val p = PeriodPredictor.predict(start, 35, start.plusDays(5))
        assertThat(p.nextDate).isEqualTo(start.plusDays(35))
        assertThat(p.daysUntil).isEqualTo(30)
    }

    // -------------------------------------------------------------- predictAdaptive

    @Test fun `adaptive with no logs falls back to profile`() {
        val p = PeriodPredictor.predictAdaptive(profile(), emptyList(), start.plusDays(10))
        assertThat(p.nextDate).isEqualTo(start.plusDays(28))
        assertThat(p.daysUntil).isEqualTo(18)
    }

    @Test fun `adaptive picks latest logged start as anchor`() {
        // Onboarding said May 1, but we've actually logged a bleed starting June 1.
        val onboardingStart = LocalDate.of(2026, 5, 1)
        val realStart = LocalDate.of(2026, 6, 1)
        val days = bleed(realStart, 4)
        val p = PeriodPredictor.predictAdaptive(
            profile = profile(last = onboardingStart, cycle = 28),
            loggedPeriodDays = days,
            today = realStart.plusDays(5),
        )
        // Anchor = realStart, cycle len falls back to 28 (only one logged start).
        assertThat(p.nextDate).isEqualTo(realStart.plusDays(28))
    }

    @Test fun `adaptive cycle length comes from rolling average of logs`() {
        // Two complete cycles: starts 28 days apart, then 26 days apart → avg 27.
        val s0 = LocalDate.of(2026, 4, 1)
        val s1 = s0.plusDays(28)
        val s2 = s1.plusDays(26)
        val days = bleed(s0, 4) + bleed(s1, 4) + bleed(s2, 3)
        val p = PeriodPredictor.predictAdaptive(
            profile = profile(last = s0, cycle = 28),
            loggedPeriodDays = days,
            today = s2.plusDays(5),
        )
        // Anchor = s2, adaptive cycle len = 27.
        assertThat(p.nextDate).isEqualTo(s2.plusDays(27))
    }
}
