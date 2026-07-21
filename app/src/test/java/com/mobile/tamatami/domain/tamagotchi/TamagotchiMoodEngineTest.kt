package com.mobile.tamatami.domain.tamagotchi

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.domain.model.Accessory
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.TamagotchiMood
import org.junit.Test
import java.time.LocalDate

class TamagotchiMoodEngineTest {

    private val today: LocalDate = LocalDate.of(2026, 6, 14)

    private fun cycle(phase: CyclePhase, dayInPhase: Int = 3): CycleSnapshot =
        CycleSnapshot(
            phase = phase,
            cycleDay = dayInPhase,
            dayInPhase = dayInPhase,
            predictedNextPeriod = today.plusDays(14),
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

    @Test fun `luteal with two glasses is sad and thirsty`() {
        val state = TamagotchiMoodEngine.derive(cycle(CyclePhase.LUTEAL), daily(water = 2))
        assertThat(state.accessories).contains(Accessory.THIRSTY_DROPLET)
        // luteal baseline 0, water 2 (<4 = goal/2) -> -1, no mood -> total -1 -> NEUTRAL
        assertThat(state.mood).isEqualTo(TamagotchiMood.NEUTRAL)
    }

    @Test fun `luteal awful mood with no water is sad with zzz`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.LUTEAL),
            daily(water = 0, mood = Mood.AWFUL),
        )
        assertThat(state.mood).isEqualTo(TamagotchiMood.SAD)
        assertThat(state.accessories).containsAtLeast(
            Accessory.THIRSTY_DROPLET, Accessory.TIRED_ZZZ,
        )
    }

    @Test fun `ovulatory great mood with hydration is glowing`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.OVULATORY),
            daily(water = 8, mood = Mood.GREAT),
        )
        assertThat(state.mood).isEqualTo(TamagotchiMood.GLOWING)
        assertThat(state.accessories).contains(Accessory.SPARKLE)
        assertThat(state.bounceHz).isEqualTo(1.4f)
    }

    @Test fun `follicular baseline with goal-met water is happy`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.FOLLICULAR),
            daily(water = 8),
        )
        // follicular +1, water +2 = 3 -> HAPPY
        assertThat(state.mood).isEqualTo(TamagotchiMood.HAPPY)
    }

    @Test fun `menstrual with flow logged earns bandaid`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 2),
            daily(water = 8, flow = PeriodFlow.MEDIUM),
        )
        assertThat(state.accessories).contains(Accessory.BANDAID)
        // menstrual + flow +1, water +2 = 3 -> HAPPY
        assertThat(state.mood).isEqualTo(TamagotchiMood.HAPPY)
    }

    @Test fun `menstrual without flow past day 1 penalised`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.MENSTRUAL, dayInPhase = 3),
            daily(water = 4),
        )
        // no water score (4 < 8 but not < 4), no mood, menstrual no-flow -1 -> -1 NEUTRAL
        assertThat(state.mood).isEqualTo(TamagotchiMood.NEUTRAL)
    }

    @Test fun `unknown phase falls through cleanly`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.UNKNOWN),
            daily(water = 8, mood = Mood.GOOD),
        )
        // water +2, mood good +1 = 3 -> HAPPY, no phase accessories
        assertThat(state.mood).isEqualTo(TamagotchiMood.HAPPY)
        assertThat(state.accessories).isEmpty()
    }

    @Test fun `glowing requires a strong day`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.FOLLICULAR),
            daily(water = 8, mood = Mood.GREAT),
        )
        // follicular +1, water +2, mood great +2 = 5 -> GLOWING
        assertThat(state.mood).isEqualTo(TamagotchiMood.GLOWING)
    }

    @Test fun `bounceHz defaults to slow for non-glowing`() {
        val state = TamagotchiMoodEngine.derive(
            cycle(CyclePhase.LUTEAL),
            daily(water = 4),
        )
        assertThat(state.bounceHz).isEqualTo(0.6f)
    }
}
