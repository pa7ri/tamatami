package com.mobile.tamatami.domain.tamagotchi

import com.mobile.tamatami.domain.model.Accessory
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.TamaExpression
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.model.TamagotchiState

/**
 * Derives today's [TamagotchiState] from the user's cycle context and what
 * they've logged today. Pure function — easy to unit test, easy to tweak.
 *
 * Scoring:
 *   water       : >= goal → +2 ; < goal/2 → −1 + THIRSTY_DROPLET
 *   mood log    : GREAT +2, GOOD +1, NEUTRAL 0, LOW −1, AWFUL −2
 *   MENSTRUAL   : flow logged → +1 + BANDAID
 *                 no flow past day 1 → −1
 *   OVULATORY   : mood >= GOOD → +1 + SPARKLE
 *   FOLLICULAR  : baseline +1
 *   LUTEAL      : mood <= LOW → TIRED_ZZZ (no score delta — luteal is tough)
 *
 * Sum → TamagotchiMood:
 *   >= 4 GLOWING ; 2..3 HAPPY ; 0..1 CONTENT ; −1..−2 NEUTRAL ; <= −3 SAD.
 *
 *   GLOWING gets a brisk 1.4 Hz bounce; everyone else 0.6 Hz.
 *
 * The animated [TamaExpression] (one Lottie per value) then follows the
 * sustained mood tier so it stays steady day to day:
 *   GLOWING → romantic/happy ; HAPPY → happy ; CONTENT → greeting ;
 *   NEUTRAL → moody ; SAD → sad/tired.
 */
object TamagotchiMoodEngine {

    fun derive(cycle: CycleSnapshot, daily: DailySnapshot): TamagotchiState {
        var score = 0
        val accessories = mutableSetOf<Accessory>()

        // --- Water ----------------------------------------------------------
        val goal = daily.waterGoal.coerceAtLeast(1)
        val glasses = daily.waterGlasses
        when {
            glasses >= goal -> score += 2
            glasses < goal / 2 -> {
                score -= 1
                accessories += Accessory.THIRSTY_DROPLET
            }
        }

        // --- Mood log -------------------------------------------------------
        when (daily.mood) {
            Mood.GREAT -> score += 2
            Mood.GOOD -> score += 1
            Mood.NEUTRAL, null -> Unit
            Mood.LOW -> score -= 1
            Mood.AWFUL -> score -= 2
        }

        // --- Phase-specific signals ----------------------------------------
        when (cycle.phase) {
            CyclePhase.MENSTRUAL -> {
                val hasFlow = daily.periodFlow != null && daily.periodFlow != PeriodFlow.NONE
                if (hasFlow) {
                    score += 1
                    accessories += Accessory.BANDAID
                } else if (cycle.dayInPhase >= 2) {
                    score -= 1
                }
            }
            CyclePhase.FOLLICULAR -> score += 1
            CyclePhase.OVULATORY -> {
                if (daily.mood == Mood.GREAT || daily.mood == Mood.GOOD) {
                    score += 1
                    accessories += Accessory.SPARKLE
                }
            }
            CyclePhase.LUTEAL -> {
                if (daily.mood == Mood.LOW || daily.mood == Mood.AWFUL) {
                    accessories += Accessory.TIRED_ZZZ
                }
            }
            CyclePhase.UNKNOWN -> Unit
        }

        val mood = when {
            score >= 4 -> TamagotchiMood.GLOWING
            score >= 2 -> TamagotchiMood.HAPPY
            score >= 0 -> TamagotchiMood.CONTENT
            score >= -2 -> TamagotchiMood.NEUTRAL
            else -> TamagotchiMood.SAD
        }

        val expression = deriveExpression(mood, accessories, cycle)

        return TamagotchiState(
            mood = mood,
            accessories = accessories.toSet(),
            bounceHz = if (mood == TamagotchiMood.GLOWING) 1.4f else 0.6f,
            expression = expression,
        )
    }

    /**
     * Picks the animated expression from the sustained mood tier, so the mascot
     * doesn't flip animations on every small signal change. Each of the five
     * mood tiers maps to one steady expression; ROMANTIC and SAD_TIRED are
     * reserved for the strongest states (GLOWING / SAD) rather than firing on a
     * single good or neutral day.
     */
    private fun deriveExpression(
        mood: TamagotchiMood,
        accessories: Set<Accessory>,
        cycle: CycleSnapshot,
    ): TamaExpression {
        // A romantic flourish only when the mascot is already glowing *and* the
        // signals back it up — not for an ordinary good mood.
        val romanticVibe = mood == TamagotchiMood.GLOWING &&
            (Accessory.HEART in accessories || Accessory.SPARKLE in accessories ||
                cycle.phase == CyclePhase.OVULATORY)
        return when {
            romanticVibe -> TamaExpression.ROMANTIC
            mood == TamagotchiMood.GLOWING || mood == TamagotchiMood.HAPPY -> TamaExpression.HAPPY
            mood == TamagotchiMood.CONTENT -> TamaExpression.GREETING
            mood == TamagotchiMood.NEUTRAL -> TamaExpression.MOODY
            else -> TamaExpression.SAD_TIRED // SAD
        }
    }
}
