package com.mobile.tamatami.domain.medication

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * How often a medication is due — the *which days* half of a schedule
 * ([TimeOfDay] slots are the *when on a due day* half).
 *
 * Persisted as two plain [Int] columns (avoiding a type-converter):
 *  - [FrequencyKind.ordinal] in `frequencyKind`
 *  - a kind-specific payload in `frequencyValue`:
 *      DAILY        → unused (0)
 *      EVERY_N_DAYS → the interval N (>= 1)
 *      WEEKLY       → a weekday bitmask (bit per [DayOfWeek.ordinal], Mon=0)
 *
 * [MedicationFrequency.encode]/[decode] are the one place that mapping lives,
 * mirroring [MedicationSchedule]'s slot bitmask.
 */
enum class FrequencyKind { DAILY, EVERY_N_DAYS, WEEKLY }

data class MedicationFrequency(
    val kind: FrequencyKind,
    /** Interval N for [FrequencyKind.EVERY_N_DAYS]; otherwise ignored. */
    val intervalDays: Int = 1,
    /** Selected weekdays for [FrequencyKind.WEEKLY]; otherwise ignored. */
    val weekdays: Set<DayOfWeek> = emptySet(),
) {
    /** The `frequencyValue` column payload for this frequency. */
    val value: Int
        get() = when (kind) {
            FrequencyKind.DAILY -> 0
            FrequencyKind.EVERY_N_DAYS -> intervalDays.coerceAtLeast(1)
            FrequencyKind.WEEKLY -> weekdaysMask(weekdays)
        }

    /**
     * Whether a dose is due on [date], given the medication's [createdDate]
     * (the anchor day for EVERY_N_DAYS counting).
     */
    fun isDueOn(date: LocalDate, createdDate: LocalDate): Boolean = when (kind) {
        FrequencyKind.DAILY -> true
        FrequencyKind.EVERY_N_DAYS -> {
            val n = intervalDays.coerceAtLeast(1)
            val delta = createdDate.daysUntil(date)
            delta >= 0 && delta % n == 0
        }
        FrequencyKind.WEEKLY -> weekdays.isEmpty() || date.dayOfWeek in weekdays
    }

    /** Short human summary for the medication card. */
    fun summary(): String = when (kind) {
        FrequencyKind.DAILY -> "Every day"
        FrequencyKind.EVERY_N_DAYS ->
            if (intervalDays <= 1) "Every day" else "Every $intervalDays days"
        FrequencyKind.WEEKLY ->
            if (weekdays.isEmpty()) "Weekly"
            else DayOfWeek.entries
                .filter { it in weekdays }
                .joinToString(", ") { it.shortLabel() }
    }

    companion object {
        val Daily = MedicationFrequency(FrequencyKind.DAILY)

        fun decode(kind: Int, value: Int): MedicationFrequency {
            val k = FrequencyKind.entries.getOrElse(kind) { FrequencyKind.DAILY }
            return when (k) {
                FrequencyKind.DAILY -> Daily
                FrequencyKind.EVERY_N_DAYS ->
                    MedicationFrequency(k, intervalDays = value.coerceAtLeast(1))
                FrequencyKind.WEEKLY ->
                    MedicationFrequency(k, weekdays = weekdaysOf(value))
            }
        }

        private fun weekdaysMask(days: Set<DayOfWeek>): Int =
            days.fold(0) { acc, d -> acc or (1 shl d.ordinal) }

        private fun weekdaysOf(mask: Int): Set<DayOfWeek> =
            DayOfWeek.entries.filterTo(mutableSetOf()) { mask and (1 shl it.ordinal) != 0 }
    }
}

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
    else -> name.take(3)
}
