package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Derives cycle history from logged period days.
 *
 * A "period start" is a logged period day with no logged period day in the
 * preceding [gapDays] — that gap is what separates one bleed from the next.
 * From consecutive starts we get cycle lengths; averaging the last [window]
 * of those produces an **adaptive** cycle length that beats whatever the
 * user typed during onboarding once they've logged a few cycles.
 *
 * Pure — no flows, no DAOs, easy to unit-test.
 */
object CycleHistory {

    /**
     * Detect period start dates in ascending order. Defensive against unsorted
     * input — the DAO already orders by `date DESC`, but we sort here so the
     * function is order-independent.
     */
    fun detectStarts(
        loggedPeriodDays: List<PeriodDayEntity>,
        gapDays: Int = 5,
    ): List<LocalDate> {
        if (loggedPeriodDays.isEmpty()) return emptyList()
        val dates = loggedPeriodDays.map { it.date }.distinct().sorted()
        val starts = ArrayList<LocalDate>(dates.size)
        var prev: LocalDate? = null
        for (d in dates) {
            if (prev == null || ChronoUnit.DAYS.between(prev, d) > gapDays) {
                starts += d
            }
            prev = d
        }
        return starts
    }

    /**
     * Average cycle length (days between consecutive starts) over the most
     * recent [window] cycles. Falls back to [fallback] when there are fewer
     * than two starts (we need a difference). The result is rounded to the
     * nearest int — `CyclePhaseCalculator` already clamps to its safe range.
     */
    fun avgCycleLength(
        starts: List<LocalDate>,
        fallback: Int,
        window: Int = 6,
    ): Int {
        if (starts.size < 2) return fallback
        val sorted = starts.sorted()
        val deltas = sorted.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
        val recent = deltas.takeLast(window)
        return recent.average().let { Math.round(it).toInt() }
    }
}
