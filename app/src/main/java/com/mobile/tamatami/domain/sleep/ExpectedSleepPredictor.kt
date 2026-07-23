package com.mobile.tamatami.domain.sleep

/**
 * Predicts the sleep quality the user can *expect tonight* from their recent
 * logged nights — a simple forward-looking average, not a per-night estimate.
 *
 * Pure function over already-computed per-night qualities (see
 * [SleepQualityEstimator.estimate]); the caller supplies the recent history.
 */
object ExpectedSleepPredictor {

    /** Fewer than this many logged nights and we don't claim a prediction. */
    const val MIN_NIGHTS = 3

    data class Prediction(
        val expected: SleepQuality,
        /** How many nights fed the prediction. */
        val basedOnNights: Int,
        /** Rough confidence from sample size + how consistent the nights were. */
        val confidence: Confidence,
    )

    enum class Confidence { LOW, MEDIUM, HIGH }

    /**
     * @param recentQualities per-night qualities, most-recent-first, already
     *   computed from history. Returns null when there's too little data.
     */
    fun predict(recentQualities: List<SleepQuality>): Prediction? {
        if (recentQualities.size < MIN_NIGHTS) return null

        // Average the ordinal scores (POOR=0 .. GREAT=3), rounded to a level.
        val scores = recentQualities.map { it.ordinal }
        val mean = scores.average()
        val expected = SleepQuality.entries[mean.roundToIntHalfUp().coerceIn(0, 3)]

        // Spread → confidence: tight cluster + more nights = higher confidence.
        val variance = scores.sumOf { (it - mean) * (it - mean) } / scores.size
        val confidence = when {
            recentQualities.size >= 7 && variance <= 0.5 -> Confidence.HIGH
            recentQualities.size >= 5 && variance <= 1.0 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return Prediction(expected, recentQualities.size, confidence)
    }

    private fun Double.roundToIntHalfUp(): Int = kotlin.math.floor(this + 0.5).toInt()
}
