package com.mobile.tamatami.domain.sleep

/**
 * Estimates [SleepQuality] from how long the user slept and their own quick
 * [SleepRating]. Pure function — no Android, no clock — so it unit-tests easily.
 *
 * Duration is scored against a 7–9h "ideal" band: too little or far too much
 * both drop the score. The self-rating then nudges the result up or down by a
 * level, so a short-but-restful night and a long-but-poor night both land
 * somewhere sensible in the middle.
 */
object SleepQualityEstimator {

    private const val IDEAL_MIN = 7 * 60   // 420
    private const val IDEAL_MAX = 9 * 60   // 540

    fun estimate(minutesAsleep: Int, rating: SleepRating): SleepQuality {
        // Base score 0..3 from duration.
        val durationScore = when {
            minutesAsleep in IDEAL_MIN..IDEAL_MAX -> 3          // 7–9h
            minutesAsleep in (6 * 60) until IDEAL_MIN -> 2      // 6–7h
            minutesAsleep in (IDEAL_MAX + 1)..(10 * 60) -> 2    // 9–10h (a bit much)
            minutesAsleep in (5 * 60) until (6 * 60) -> 1       // 5–6h
            else -> 0                                           // <5h or >10h
        }

        // Self-rating nudges the score by ±1 (OKAY is neutral).
        val ratingDelta = when (rating) {
            SleepRating.RESTFUL -> 1
            SleepRating.OKAY -> 0
            SleepRating.POOR -> -1
        }

        return when ((durationScore + ratingDelta).coerceIn(0, 3)) {
            3 -> SleepQuality.GREAT
            2 -> SleepQuality.GOOD
            1 -> SleepQuality.FAIR
            else -> SleepQuality.POOR
        }
    }

    /**
     * Minutes asleep from bed/wake times expressed as minute-of-day (0..1439).
     * Handles the common past-midnight case (wake earlier in the day than bed).
     */
    fun durationMinutes(bedMinuteOfDay: Int, wakeMinuteOfDay: Int): Int {
        val raw = wakeMinuteOfDay - bedMinuteOfDay
        return if (raw <= 0) raw + 24 * 60 else raw
    }
}
