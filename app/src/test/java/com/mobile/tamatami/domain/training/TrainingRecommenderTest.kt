package com.mobile.tamatami.domain.training

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.domain.model.CyclePhase
import org.junit.Test

class TrainingRecommenderTest {

    @Test fun `menstrual recommends rest or yoga at low intensity`() {
        val r = TrainingRecommender.recommend(CyclePhase.MENSTRUAL, energy = null)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.LOW)
        assertThat(r.suggestedTypes).contains(WorkoutType.REST)
        assertThat(r.suggestedTypes).contains(WorkoutType.YOGA)
    }

    @Test fun `ovulatory peaks at high intensity strength or HIIT`() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = null)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.HIGH)
        assertThat(r.suggestedTypes).containsAtLeast(WorkoutType.STRENGTH, WorkoutType.HIIT)
    }

    @Test fun `follicular and luteal default to moderate`() {
        val f = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        val l = TrainingRecommender.recommend(CyclePhase.LUTEAL, energy = null)
        assertThat(f.intensity).isEqualTo(WorkoutIntensity.MODERATE)
        assertThat(l.intensity).isEqualTo(WorkoutIntensity.MODERATE)
    }

    @Test fun `low energy downshifts ovulatory to moderate cardio`() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = 2)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.MODERATE)
        // HIIT swapped to CARDIO, STRENGTH swapped to YOGA on downshift.
        assertThat(r.suggestedTypes).doesNotContain(WorkoutType.HIIT)
        assertThat(r.suggestedTypes).doesNotContain(WorkoutType.STRENGTH)
    }

    @Test fun `energy 5 in luteal allows step up to high`() {
        val r = TrainingRecommender.recommend(CyclePhase.LUTEAL, energy = 5)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.HIGH)
    }

    @Test fun `null energy leaves phase default unchanged`() {
        val base = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        val again = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        assertThat(again).isEqualTo(base)
    }

    @Test fun `energy 5 outside luteal does not exceed phase default`() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = 5)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.HIGH)
    }

    @Test fun `unknown phase falls back to low intensity walk-yoga`() {
        val r = TrainingRecommender.recommend(CyclePhase.UNKNOWN, energy = null)
        assertThat(r.intensity).isEqualTo(WorkoutIntensity.LOW)
        assertThat(r.suggestedTypes).contains(WorkoutType.WALK)
    }
}
