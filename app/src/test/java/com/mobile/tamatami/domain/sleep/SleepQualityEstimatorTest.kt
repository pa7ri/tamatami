package com.mobile.tamatami.domain.sleep

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SleepQualityEstimatorTest {

    @Test
    fun `ideal duration with restful rating is great`() {
        assertThat(SleepQualityEstimator.estimate(8 * 60, SleepRating.RESTFUL))
            .isEqualTo(SleepQuality.GREAT)
    }

    @Test
    fun `ideal duration with okay rating is great`() {
        // durationScore 3, capped at 3 regardless of neutral rating.
        assertThat(SleepQualityEstimator.estimate(8 * 60, SleepRating.OKAY))
            .isEqualTo(SleepQuality.GREAT)
    }

    @Test
    fun `ideal duration but poor rating drops a level`() {
        assertThat(SleepQualityEstimator.estimate(8 * 60, SleepRating.POOR))
            .isEqualTo(SleepQuality.GOOD)
    }

    @Test
    fun `very short sleep is poor even if rated restful`() {
        // 4h -> durationScore 0, +1 restful -> FAIR (1)
        assertThat(SleepQualityEstimator.estimate(4 * 60, SleepRating.RESTFUL))
            .isEqualTo(SleepQuality.FAIR)
        // 4h poor -> stays POOR
        assertThat(SleepQualityEstimator.estimate(4 * 60, SleepRating.POOR))
            .isEqualTo(SleepQuality.POOR)
    }

    @Test
    fun `six to seven hours is good when okay`() {
        assertThat(SleepQualityEstimator.estimate(6 * 60 + 30, SleepRating.OKAY))
            .isEqualTo(SleepQuality.GOOD)
    }

    @Test
    fun `oversleeping past ten hours scores low`() {
        assertThat(SleepQualityEstimator.estimate(11 * 60, SleepRating.OKAY))
            .isEqualTo(SleepQuality.POOR)
    }

    @Test
    fun `duration handles crossing midnight`() {
        // Bed 23:00 (1380), wake 07:00 (420) -> 8h.
        assertThat(SleepQualityEstimator.durationMinutes(1380, 420)).isEqualTo(8 * 60)
    }

    @Test
    fun `duration same-day nap`() {
        // Bed 13:00 (780), wake 14:30 (870) -> 90 min.
        assertThat(SleepQualityEstimator.durationMinutes(780, 870)).isEqualTo(90)
    }

    @Test
    fun `duration equal times is a full day`() {
        // Degenerate but must not be zero/negative.
        assertThat(SleepQualityEstimator.durationMinutes(600, 600)).isEqualTo(24 * 60)
    }
}
