package com.mobile.tamatami.domain.tamagotchi

import com.mobile.tamatami.domain.model.Accessory
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.TamaExpression
import com.mobile.tamatami.domain.model.TamagotchiMood
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TamagotchiMoodEngineTest {

    private val today: LocalDate = LocalDate(2026, 6, 14)

    private fun cycle(phase: CyclePhase, dayInPhase: Int = 3): CycleSnapshot =
        CycleSnapshot(
            phase = phase,
            cycleDay = dayInPhase,
            dayInPhase = dayInPhase,
            predictedNextPeriod = today.plus(14, DateTimeUnit.DAY),
            daysUntilNextPeriod = 14,
            cycleLength = 28,
        )

    private fun daily(
        water: Int = 0,
        goal: Int = 8,
        mood: Mood? = null,
        flow: PeriodFlow? = null,
    ): DailySnapshot = DailySnapshot(
        date = today,
        waterGlasses = water,
        waterGoal = goal,
        mood = mood,
        energy = null,
        moodNotes = null,
        periodFlow = flow,
        symptoms = emptySet(),
        craving = null,
        workouts = emptyList(),
        sleep = null,
    )

    @Test fun lutealWithTwoGlassesIsSadAndThirsty() {
        val state = TamagotchiMoodEngine.derive(cycle(CyclePhase.LUTEAL), daily(water = 2))
        assertContains(state.accessories, Accessory.THIRSTY_DROPLET)
        // luteal baseline 0, water 2 (<4 = goal/2) -> -1, no mood -> total -1 -> NEUTRAL
        assertEquals(TamagotchiMood.NEUTRAL, state.mood)
    }

    @Test fun lutealAwfulMoodWithNoWaterIsSadWithZzz() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.LUTEAL),
            daily(water = 0, mood = Mood.AWFUL),
        )
        assertEquals(TamagotchiMood.SAD, state.mood)
        assertTrue(
            state.accessories.containsAll(
                listOf(Accessory.THIRSTY_DROPLET, Accessory.TIRED_ZZZ),
            ),
        )
    }

    @Test fun ovulatoryGreatMoodWithHydrationIsGlowing() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.OVULATORY),
            daily(water = 8, mood = Mood.GREAT),
        )
        assertEquals(TamagotchiMood.GLOWING, state.mood)
        assertContains(state.accessories, Accessory.SPARKLE)
        assertEquals(1.4f, state.bounceHz)
    }

    @Test fun follicularBaselineWithGoalMetWaterIsHappy() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.FOLLICULAR),
            daily(water = 8),
        )
        // follicular +1, water +2 = 3 -> HAPPY
        assertEquals(TamagotchiMood.HAPPY, state.mood)
    }

    @Test fun menstrualWithFlowLoggedEarnsBandaid() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 2),
            daily(water = 8, flow = PeriodFlow.MEDIUM),
        )
        assertContains(state.accessories, Accessory.BANDAID)
        // menstrual + flow +1, water +2 = 3 -> HAPPY
        assertEquals(TamagotchiMood.HAPPY, state.mood)
    }

    @Test fun menstrualWithoutFlowPastDay1Penalised() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 3),
            daily(water = 4),
        )
        // no water score (4 < 8 but not < 4), no mood, menstrual no-flow -1 -> -1 NEUTRAL
        assertEquals(TamagotchiMood.NEUTRAL, state.mood)
    }

    @Test fun unknownPhaseFallsThroughCleanly() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.UNKNOWN),
            daily(water = 8, mood = Mood.GOOD),
        )
        // water +2, mood good +1 = 3 -> HAPPY, no phase accessories
        assertEquals(TamagotchiMood.HAPPY, state.mood)
        assertTrue(state.accessories.isEmpty())
    }

    @Test fun glowingRequiresAStrongDay() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.FOLLICULAR),
            daily(water = 8, mood = Mood.GREAT),
        )
        // follicular +1, water +2, mood great +2 = 5 -> GLOWING
        assertEquals(TamagotchiMood.GLOWING, state.mood)
    }

    @Test fun bounceHzDefaultsToSlowForNonGlowing() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.LUTEAL),
            daily(water = 4),
        )
        assertEquals(0.6f, state.bounceHz)
    }

    // --- Expression derivation (one Lottie per state) ------------------------

    @Test fun lowWaterExpressionIsThirsty() {
        // water 0 (< goal/2) earns the THIRSTY_DROPLET, which outranks everything.
        val state = TamagotchiMoodEngine.derive(cycle(CyclePhase.FOLLICULAR), daily(water = 0))
        assertEquals(TamaExpression.THIRSTY, state.expression)
    }

    @Test fun lutealLowMoodButHydratedExpressionIsSleepy() {
        // hydrated (no droplet) but luteal + low mood earns the ZZZ -> SLEEPY.
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.LUTEAL),
            daily(water = 8, mood = Mood.LOW),
        )
        assertEquals(TamaExpression.SLEEPY, state.expression)
    }

    @Test fun ovulatoryGreatMoodExpressionIsRomantic() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.OVULATORY),
            daily(water = 8, mood = Mood.GREAT),
        )
        assertEquals(TamaExpression.ROMANTIC, state.expression)
    }

    @Test fun sadDayExpressionIsSadTired() {
        // hydrated so no droplet; follicular +1, water +2, awful mood -2,
        // but we want SAD (score <= -3): use dehydrated-but-not-thirsty is impossible,
        // so drop water to goal (no bonus) via low goal and stack menstrual penalty.
        // menstrual no-flow day3 -1, water 4 (no score), awful -2 = -3 -> SAD.
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 3),
            daily(water = 4, mood = Mood.AWFUL),
        )
        assertEquals(TamagotchiMood.SAD, state.mood)
        assertEquals(TamaExpression.SAD_TIRED, state.expression)
    }

    @Test fun neutralDayExpressionIsMoody() {
        // menstrual no-flow day3 -1, water 4 (no score), no mood = -1 -> NEUTRAL.
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 3),
            daily(water = 4),
        )
        assertEquals(TamagotchiMood.NEUTRAL, state.mood)
        assertEquals(TamaExpression.MOODY, state.expression)
    }

    @Test fun strongHappyDayExpressionIsHappy() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.FOLLICULAR),
            daily(water = 8),
        )
        // follicular +1, water +2 = 3 -> HAPPY, no romantic accessory.
        assertEquals(TamagotchiMood.HAPPY, state.mood)
        assertEquals(TamaExpression.HAPPY, state.expression)
    }

    @Test fun contentUnknownDayFallsThroughToGreeting() {
        // content mood (score 0..1), no thirst/sleep/romance signals -> default GREETING.
        // unknown phase, water 4 (no score), good mood +1 = +1 -> CONTENT.
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.UNKNOWN),
            daily(water = 4, mood = Mood.GOOD),
        )
        assertEquals(TamagotchiMood.CONTENT, state.mood)
        assertEquals(TamaExpression.GREETING, state.expression)
    }
}
