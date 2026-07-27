package com.mobile.tamatami.domain.training

import com.mobile.tamatami.domain.model.CyclePhase
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrainingRecommenderTest {

    @Test fun menstrualRecommendsRestOrYogaAtLowIntensity() {
        val r = TrainingRecommender.recommend(CyclePhase.MENSTRUAL, energy = null)
        assertEquals(WorkoutIntensity.LOW, r.intensity)
        assertContains(r.suggestedTypes, WorkoutType.REST)
        assertContains(r.suggestedTypes, WorkoutType.YOGA)
    }

    @Test fun ovulatoryPeaksAtHighIntensityStrengthOrHiit() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = null)
        assertEquals(WorkoutIntensity.HIGH, r.intensity)
        assertTrue(r.suggestedTypes.containsAll(listOf(WorkoutType.STRENGTH, WorkoutType.HIIT)))
    }

    @Test fun follicularAndLutealDefaultToModerate() {
        val f = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        val l = TrainingRecommender.recommend(CyclePhase.LUTEAL, energy = null)
        assertEquals(WorkoutIntensity.MODERATE, f.intensity)
        assertEquals(WorkoutIntensity.MODERATE, l.intensity)
    }

    @Test fun lowEnergyDownshiftsOvulatoryToModerateCardio() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = 2)
        assertEquals(WorkoutIntensity.MODERATE, r.intensity)
        // HIIT swapped to CARDIO, STRENGTH swapped to YOGA on downshift.
        assertFalse(WorkoutType.HIIT in r.suggestedTypes)
        assertFalse(WorkoutType.STRENGTH in r.suggestedTypes)
    }

    @Test fun energy5InLutealAllowsStepUpToHigh() {
        val r = TrainingRecommender.recommend(CyclePhase.LUTEAL, energy = 5)
        assertEquals(WorkoutIntensity.HIGH, r.intensity)
    }

    @Test fun nullEnergyLeavesPhaseDefaultUnchanged() {
        val base = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        val again = TrainingRecommender.recommend(CyclePhase.FOLLICULAR, energy = null)
        assertEquals(base, again)
    }

    @Test fun energy5OutsideLutealDoesNotExceedPhaseDefault() {
        val r = TrainingRecommender.recommend(CyclePhase.OVULATORY, energy = 5)
        assertEquals(WorkoutIntensity.HIGH, r.intensity)
    }

    @Test fun unknownPhaseFallsBackToLowIntensityWalkYoga() {
        val r = TrainingRecommender.recommend(CyclePhase.UNKNOWN, energy = null)
        assertEquals(WorkoutIntensity.LOW, r.intensity)
        assertContains(r.suggestedTypes, WorkoutType.WALK)
    }
}
