package com.mobile.tamatami.notifications

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.data.db.entity.MedicationEntity
import com.mobile.tamatami.domain.medication.MedicationSchedule
import com.mobile.tamatami.domain.medication.TimeOfDay
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class ReminderEvaluatorTest {

    private fun med(id: Long, slots: Set<TimeOfDay>) = MedicationEntity(
        id = id,
        name = "med$id",
        dosesPerDay = slots.size,
        slotsMask = MedicationSchedule.maskOf(slots),
        active = true,
        createdAt = Instant.ofEpochMilli(0),
    )

    @Test
    fun `period and water branches delegate to ReminderLogic`() {
        // 1 day away + behind pace mid-morning with 0 glasses → both fire.
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = 1,
            glasses = 0,
            goal = 8,
            now = LocalTime.of(15, 0),
            activeMeds = emptyList(),
            takenByMed = emptyMap(),
        )
        assertThat(decision.period).isTrue()
        assertThat(decision.water).isTrue()
        assertThat(decision.pillSlots).isEmpty()
    }

    @Test
    fun `no period reminder when not the day before`() {
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = 3,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(15, 0),
            activeMeds = emptyList(),
            takenByMed = emptyMap(),
        )
        assertThat(decision.period).isFalse()
        assertThat(decision.water).isFalse()
    }

    @Test
    fun `pill fires for a med whose passed slot is untaken`() {
        val morningMed = med(1, setOf(TimeOfDay.MORNING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = null,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(10, 0), // past MORNING (09:00), before AFTERNOON
            activeMeds = listOf(morningMed),
            takenByMed = emptyMap(),
        )
        assertThat(decision.pillSlots).containsExactly(1L, TimeOfDay.MORNING)
    }

    @Test
    fun `pill does not fire once the slot is logged as taken`() {
        val morningMed = med(1, setOf(TimeOfDay.MORNING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = null,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(10, 0),
            activeMeds = listOf(morningMed),
            takenByMed = mapOf(1L to setOf(TimeOfDay.MORNING)),
        )
        assertThat(decision.pillSlots).isEmpty()
    }

    @Test
    fun `pill does not fire for a med not scheduled in the current slot`() {
        // Only EVENING scheduled, but it's mid-afternoon: current slot is AFTERNOON.
        val eveningMed = med(1, setOf(TimeOfDay.EVENING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = null,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(15, 0), // past AFTERNOON (14:00), before EVENING
            activeMeds = listOf(eveningMed),
            takenByMed = emptyMap(),
        )
        assertThat(decision.pillSlots).isEmpty()
    }

    @Test
    fun `no pill slot before any slot time has passed`() {
        val morningMed = med(1, setOf(TimeOfDay.MORNING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = null,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(7, 0), // before MORNING (09:00)
            activeMeds = listOf(morningMed),
            takenByMed = emptyMap(),
        )
        assertThat(decision.pillSlots).isEmpty()
    }

    @Test
    fun `disabled period reminder is suppressed while others still fire`() {
        val morningMed = med(1, setOf(TimeOfDay.MORNING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = 1, // would otherwise fire
            glasses = 0,
            goal = 8,
            now = LocalTime.of(10, 0),
            activeMeds = listOf(morningMed),
            takenByMed = emptyMap(),
            remindPeriod = false,
        )
        assertThat(decision.period).isFalse()
        // Water + pills unaffected.
        assertThat(decision.water).isTrue()
        assertThat(decision.pillSlots).containsExactly(1L, TimeOfDay.MORNING)
    }

    @Test
    fun `disabled water reminder is suppressed while others still fire`() {
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = 1,
            glasses = 0, // would otherwise be behind pace
            goal = 8,
            now = LocalTime.of(15, 0),
            activeMeds = emptyList(),
            takenByMed = emptyMap(),
            remindWater = false,
        )
        assertThat(decision.water).isFalse()
        assertThat(decision.period).isTrue()
    }

    @Test
    fun `disabled pill reminder yields no pill slots even when a slot is due`() {
        val morningMed = med(1, setOf(TimeOfDay.MORNING))
        val decision = ReminderEvaluator.evaluate(
            daysUntilNextPeriod = null,
            glasses = 8,
            goal = 8,
            now = LocalTime.of(10, 0), // past MORNING, untaken
            activeMeds = listOf(morningMed),
            takenByMed = emptyMap(),
            remindPills = false,
        )
        assertThat(decision.pillSlots).isEmpty()
    }
}
