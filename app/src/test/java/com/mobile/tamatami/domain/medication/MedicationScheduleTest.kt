package com.mobile.tamatami.domain.medication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MedicationScheduleTest {

    @Test
    fun `mask round-trips through slots`() {
        for (slots in listOf(
            emptySet(),
            setOf(TimeOfDay.MORNING),
            setOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            TimeOfDay.entries.toSet(),
        )) {
            val mask = MedicationSchedule.maskOf(slots)
            assertThat(MedicationSchedule.slotsOf(mask)).isEqualTo(slots)
        }
    }

    @Test
    fun `slots decode from known bit positions`() {
        // MORNING=bit0, AFTERNOON=bit1, EVENING=bit2
        assertThat(MedicationSchedule.slotsOf(0b101))
            .containsExactly(TimeOfDay.MORNING, TimeOfDay.EVENING)
    }

    @Test
    fun `adherence counts only scheduled slots that were taken`() {
        val a = MedicationSchedule.adherence(
            scheduledSlots = setOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            takenSlots = setOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
        )
        assertThat(a.taken).isEqualTo(1)
        assertThat(a.expected).isEqualTo(2)
        assertThat(a.complete).isFalse()
        assertThat(a.fraction).isEqualTo(0.5f)
    }

    @Test
    fun `adherence is complete when all scheduled slots taken`() {
        val slots = setOf(TimeOfDay.MORNING, TimeOfDay.EVENING)
        val a = MedicationSchedule.adherence(scheduledSlots = slots, takenSlots = slots)
        assertThat(a.complete).isTrue()
        assertThat(a.fraction).isEqualTo(1f)
    }

    @Test
    fun `no scheduled slots means nothing expected`() {
        val a = MedicationSchedule.adherence(emptySet(), setOf(TimeOfDay.MORNING))
        assertThat(a.expected).isEqualTo(0)
        assertThat(a.complete).isFalse()
        assertThat(a.fraction).isEqualTo(0f)
    }
}
