package com.mobile.tamatami.domain.sleep

import kotlin.test.Test
import kotlin.test.assertEquals

class SleepQualityEstimatorTest {

    @Test
    fun idealDurationWithRestfulRatingIsGreat() {
        assertEquals(
            SleepQuality.GREAT,
            SleepQualityEstimator.estimate(8 * 60, SleepRating.RESTFUL),
        )
    }

    @Test
    fun idealDurationWithOkayRatingIsGreat() {
        // durationScore 3, capped at 3 regardless of neutral rating.
        assertEquals(
            SleepQuality.GREAT,
            SleepQualityEstimator.estimate(8 * 60, SleepRating.OKAY),
        )
    }

    @Test
    fun idealDurationButPoorRatingDropsALevel() {
        assertEquals(
            SleepQuality.GOOD,
            SleepQualityEstimator.estimate(8 * 60, SleepRating.POOR),
        )
    }

    @Test
    fun veryShortSleepIsPoorEvenIfRatedRestful() {
        // 4h -> durationScore 0, +1 restful -> FAIR (1)
        assertEquals(
            SleepQuality.FAIR,
            SleepQualityEstimator.estimate(4 * 60, SleepRating.RESTFUL),
        )
        // 4h poor -> stays POOR
        assertEquals(
            SleepQuality.POOR,
            SleepQualityEstimator.estimate(4 * 60, SleepRating.POOR),
        )
    }

    @Test
    fun sixToSevenHoursIsGoodWhenOkay() {
        assertEquals(
            SleepQuality.GOOD,
            SleepQualityEstimator.estimate(6 * 60 + 30, SleepRating.OKAY),
        )
    }

    @Test
    fun oversleepingPastTenHoursScoresLow() {
        assertEquals(
            SleepQuality.POOR,
            SleepQualityEstimator.estimate(11 * 60, SleepRating.OKAY),
        )
    }

    @Test
    fun durationHandlesCrossingMidnight() {
        // Bed 23:00 (1380), wake 07:00 (420) -> 8h.
        assertEquals(8 * 60, SleepQualityEstimator.durationMinutes(1380, 420))
    }

    @Test
    fun durationSameDayNap() {
        // Bed 13:00 (780), wake 14:30 (870) -> 90 min.
        assertEquals(90, SleepQualityEstimator.durationMinutes(780, 870))
    }

    @Test
    fun durationEqualTimesIsAFullDay() {
        // Degenerate but must not be zero/negative.
        assertEquals(24 * 60, SleepQualityEstimator.durationMinutes(600, 600))
    }
}
