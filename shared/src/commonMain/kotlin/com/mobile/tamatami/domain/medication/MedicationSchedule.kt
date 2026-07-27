package com.mobile.tamatami.domain.medication

/**
 * Pure helpers for the pill tracker.
 *
 * A medication's scheduled slots are persisted as an [Int] bitmask (one bit per
 * [TimeOfDay] ordinal) so we avoid a list type-converter. This object is the one
 * place that mask is encoded/decoded, plus the small adherence calculation the
 * Pills UI shows.
 */
object MedicationSchedule {

    /** Decode a slot bitmask into the set of [TimeOfDay]s it represents. */
    fun slotsOf(mask: Int): Set<TimeOfDay> =
        TimeOfDay.entries.filterTo(mutableSetOf()) { mask and (1 shl it.ordinal) != 0 }

    /** Encode a set of [TimeOfDay]s into a slot bitmask. */
    fun maskOf(slots: Set<TimeOfDay>): Int =
        slots.fold(0) { acc, slot -> acc or (1 shl slot.ordinal) }

    data class Adherence(
        val taken: Int,
        val expected: Int,
    ) {
        val complete: Boolean get() = expected > 0 && taken >= expected
        /** 0f..1f; 0 when nothing is expected (nothing to be behind on). */
        val fraction: Float get() = if (expected == 0) 0f else (taken.toFloat() / expected).coerceIn(0f, 1f)
    }

    /**
     * How many of today's scheduled slots have been taken.
     *
     * @param scheduledSlots the medication's configured slots (from [slotsOf]).
     * @param takenSlots the slots already logged as taken today.
     */
    fun adherence(scheduledSlots: Set<TimeOfDay>, takenSlots: Set<TimeOfDay>): Adherence =
        Adherence(
            taken = (scheduledSlots intersect takenSlots).size,
            expected = scheduledSlots.size,
        )
}
