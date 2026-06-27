package com.mobile.tamatami.domain.cycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class PeriodPredictorTest {

    private val start = LocalDate.of(2026, 6, 1)

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
}
