package com.mobile.tamatami.notifications

import com.mobile.tamatami.data.db.entity.MedicationEntity
import com.mobile.tamatami.domain.medication.MedicationSchedule
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.domain.reminders.ReminderLogic
import kotlinx.datetime.LocalTime

/**
 * Pure orchestration for the reminder worker: given the day's already-read
 * snapshots, decide which of the three reminders should fire. Every yes/no is
 * delegated to [ReminderLogic] — this object only wires the inputs together so
 * the worker stays a thin Android shell and the branching stays unit-testable.
 */
object ReminderEvaluator {

    data class Decision(
        val period: Boolean,
        val water: Boolean,
        /** Medications whose current slot has passed and isn't logged yet. */
        val pillSlots: Map<Long, TimeOfDay>,
    )

    /**
     * @param daysUntilNextPeriod from the cycle snapshot (null if unknown).
     * @param glasses water glasses logged today.
     * @param goal daily water goal.
     * @param now current local time.
     * @param activeMeds active medications to consider.
     * @param takenByMed slots already logged as taken today, keyed by medication id.
     * @param remindPeriod whether period reminders are enabled by the user.
     * @param remindWater whether water reminders are enabled by the user.
     * @param remindPills whether medication reminders are enabled by the user.
     */
    fun evaluate(
        daysUntilNextPeriod: Int?,
        glasses: Int,
        goal: Int,
        now: LocalTime,
        activeMeds: List<MedicationEntity>,
        takenByMed: Map<Long, Set<TimeOfDay>>,
        remindPeriod: Boolean = true,
        remindWater: Boolean = true,
        remindPills: Boolean = true,
    ): Decision {
        val slot = ReminderLogic.currentPillSlot(now)
        val pillSlots = if (!remindPills || slot == null) {
            emptyMap()
        } else {
            activeMeds
                .filter { slot in MedicationSchedule.slotsOf(it.slotsMask) }
                .filter { slot !in (takenByMed[it.id] ?: emptySet()) }
                .associate { it.id to slot }
        }

        return Decision(
            period = remindPeriod && ReminderLogic.isPeriodReminderDue(daysUntilNextPeriod),
            water = remindWater && ReminderLogic.isWaterBehindPace(glasses, goal, now),
            pillSlots = pillSlots,
        )
    }
}
