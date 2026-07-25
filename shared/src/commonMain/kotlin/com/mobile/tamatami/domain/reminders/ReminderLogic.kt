package com.mobile.tamatami.domain.reminders

import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.datetime.LocalTime

/**
 * Pure decision logic for the three reminder types. The WorkManager workers are
 * thin shells that read data off the repositories and delegate the "should I
 * notify?" question here so it can be unit-tested without Android.
 */
object ReminderLogic {

    /** Reminders only fire during these waking hours (inclusive start, exclusive end). */
    val WAKING_START: LocalTime = LocalTime(9, 0)
    val WAKING_END: LocalTime = LocalTime(21, 0)

    /** Minutes since midnight — kotlinx-datetime's LocalTime has no duration math. */
    private fun LocalTime.minuteOfDay(): Int = hour * 60 + minute

    /**
     * Period-due reminder: fire when the next expected period is exactly one day
     * away (i.e. "the day before").
     */
    fun isPeriodReminderDue(daysUntilNextPeriod: Int?): Boolean =
        daysUntilNextPeriod == 1

    /**
     * Water behind-pace: during waking hours, compare glasses drunk so far against
     * the goal pro-rated across the waking window up to [now]. Returns true when
     * the user is behind that pace and hasn't already met the daily goal.
     *
     * @param glassesSoFar glasses logged today.
     * @param goalGlasses the daily goal.
     * @param now current local time.
     */
    fun isWaterBehindPace(glassesSoFar: Int, goalGlasses: Int, now: LocalTime): Boolean {
        if (goalGlasses <= 0) return false
        if (glassesSoFar >= goalGlasses) return false
        if (now < WAKING_START || now >= WAKING_END) return false

        val windowMinutes = (WAKING_END.minuteOfDay() - WAKING_START.minuteOfDay()).toDouble()
        val elapsedMinutes = (now.minuteOfDay() - WAKING_START.minuteOfDay())
            .coerceAtLeast(0).toDouble()
        val expectedByNow = goalGlasses * (elapsedMinutes / windowMinutes)
        return glassesSoFar < expectedByNow
    }

    /**
     * The [TimeOfDay] slot whose fixed reminder time has passed as of [now], or
     * null if it's not yet time for any slot (used to decide which pill slot to
     * remind about). Picks the latest slot whose [TimeOfDay.defaultTime] is at or
     * before [now].
     */
    fun currentPillSlot(now: LocalTime): TimeOfDay? =
        TimeOfDay.entries
            .filter { now >= it.defaultTime }
            .maxByOrNull { it.defaultTime }
}
