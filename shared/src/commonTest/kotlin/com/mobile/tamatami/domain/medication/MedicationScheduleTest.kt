package com.mobile.tamatami.domain.medication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MedicationScheduleTest {

    @Test
    fun maskRoundTripsThroughSlots() {
        for (slots in listOf(
            emptySet(),
            setOf(TimeOfDay.MORNING),
            setOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            TimeOfDay.entries.toSet(),
        )) {
            val mask = MedicationSchedule.maskOf(slots)
            assertEquals(slots, MedicationSchedule.slotsOf(mask))
        }
    }

    @Test
    fun slotsDecodeFromKnownBitPositions() {
        // MORNING=bit0, AFTERNOON=bit1, EVENING=bit2
        assertEquals(
            setOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            MedicationSchedule.slotsOf(0b101).toSet(),
        )
    }

    @Test
    fun adherenceCountsOnlyScheduledSlotsThatWereTaken() {
        val a = MedicationSchedule.adherence(
            scheduledSlots = setOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            takenSlots = setOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
        )
        assertEquals(1, a.taken)
        assertEquals(2, a.expected)
        assertFalse(a.complete)
        assertEquals(0.5f, a.fraction)
    }

    @Test
    fun adherenceIsCompleteWhenAllScheduledSlotsTaken() {
        val slots = setOf(TimeOfDay.MORNING, TimeOfDay.EVENING)
        val a = MedicationSchedule.adherence(scheduledSlots = slots, takenSlots = slots)
        assertTrue(a.complete)
        assertEquals(1f, a.fraction)
    }

    @Test
    fun noScheduledSlotsMeansNothingExpected() {
        val a = MedicationSchedule.adherence(emptySet(), setOf(TimeOfDay.MORNING))
        assertEquals(0, a.expected)
        assertFalse(a.complete)
        assertEquals(0f, a.fraction)
    }
}
