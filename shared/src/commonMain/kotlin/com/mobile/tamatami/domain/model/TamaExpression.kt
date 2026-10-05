package com.mobile.tamatami.domain.model

/**
 * The mascot's animated expression — a richer, animation-oriented view of the
 * Tamagotchi's state than [TamagotchiMood]. Each value maps to one Lottie
 * animation on the home page. Derived (not persisted) by
 * [com.mobile.tamatami.domain.tamagotchi.TamagotchiMoodEngine].
 */
enum class TamaExpression {
    /** Idle / neutral greeting — the default resting state. */
    GREETING,

    /** Happy and energetic — good mood, cycle going well. */
    HAPPY,

    /** Sad / low-energy — a tough day. */
    SAD_TIRED,

    /** Off / irritable — meh mood, nothing obviously wrong. */
    MOODY,

    /** Loved-up / glowing — sparkle or heart signals. */
    ROMANTIC,
}
