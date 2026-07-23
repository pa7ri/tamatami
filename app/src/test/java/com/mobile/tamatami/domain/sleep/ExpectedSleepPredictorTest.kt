package com.mobile.tamatami.domain.sleep

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExpectedSleepPredictorTest {

    @Test
    fun `too few nights yields no prediction`() {
        assertThat(ExpectedSleepPredictor.predict(emptyList())).isNull()
        assertThat(ExpectedSleepPredictor.predict(listOf(SleepQuality.GOOD, SleepQuality.GOOD)))
            .isNull()
    }

    @Test
    fun `consistent good nights predict good with high confidence`() {
        val nights = List(7) { SleepQuality.GOOD }
        val p = ExpectedSleepPredictor.predict(nights)!!
        assertThat(p.expected).isEqualTo(SleepQuality.GOOD)
        assertThat(p.basedOnNights).isEqualTo(7)
        assertThat(p.confidence).isEqualTo(ExpectedSleepPredictor.Confidence.HIGH)
    }

    @Test
    fun `average rounds to the nearest level`() {
        // GREAT(3) + GOOD(2) + GOOD(2) -> mean 2.33 -> GOOD
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.GREAT, SleepQuality.GOOD, SleepQuality.GOOD),
        )!!
        assertThat(p.expected).isEqualTo(SleepQuality.GOOD)
    }

    @Test
    fun `wildly varying nights give low confidence`() {
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.GREAT, SleepQuality.POOR, SleepQuality.GREAT, SleepQuality.POOR),
        )!!
        assertThat(p.confidence).isEqualTo(ExpectedSleepPredictor.Confidence.LOW)
    }

    @Test
    fun `minimum three nights is enough for a prediction`() {
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.FAIR, SleepQuality.FAIR, SleepQuality.FAIR),
        )
        assertThat(p).isNotNull()
        assertThat(p!!.expected).isEqualTo(SleepQuality.FAIR)
    }
}
