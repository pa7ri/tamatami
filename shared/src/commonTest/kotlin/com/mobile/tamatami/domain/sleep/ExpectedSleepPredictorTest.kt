package com.mobile.tamatami.domain.sleep

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExpectedSleepPredictorTest {

    @Test
    fun tooFewNightsYieldsNoPrediction() {
        assertNull(ExpectedSleepPredictor.predict(emptyList()))
        assertNull(ExpectedSleepPredictor.predict(listOf(SleepQuality.GOOD, SleepQuality.GOOD)))
    }

    @Test
    fun consistentGoodNightsPredictGoodWithHighConfidence() {
        val nights = List(7) { SleepQuality.GOOD }
        val p = ExpectedSleepPredictor.predict(nights)!!
        assertEquals(SleepQuality.GOOD, p.expected)
        assertEquals(7, p.basedOnNights)
        assertEquals(ExpectedSleepPredictor.Confidence.HIGH, p.confidence)
    }

    @Test
    fun averageRoundsToTheNearestLevel() {
        // GREAT(3) + GOOD(2) + GOOD(2) -> mean 2.33 -> GOOD
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.GREAT, SleepQuality.GOOD, SleepQuality.GOOD),
        )!!
        assertEquals(SleepQuality.GOOD, p.expected)
    }

    @Test
    fun wildlyVaryingNightsGiveLowConfidence() {
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.GREAT, SleepQuality.POOR, SleepQuality.GREAT, SleepQuality.POOR),
        )!!
        assertEquals(ExpectedSleepPredictor.Confidence.LOW, p.confidence)
    }

    @Test
    fun minimumThreeNightsIsEnoughForAPrediction() {
        val p = ExpectedSleepPredictor.predict(
            listOf(SleepQuality.FAIR, SleepQuality.FAIR, SleepQuality.FAIR),
        )
        assertNotNull(p)
        assertEquals(SleepQuality.FAIR, p!!.expected)
    }
}
