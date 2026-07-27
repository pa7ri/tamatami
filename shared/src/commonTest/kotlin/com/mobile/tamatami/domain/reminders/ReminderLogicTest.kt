package com.mobile.tamatami.domain.reminders

import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReminderLogicTest {

    @Test
    fun periodReminderFiresOnlyTheDayBefore() {
        assertTrue(ReminderLogic.isPeriodReminderDue(1))
        assertFalse(ReminderLogic.isPeriodReminderDue(0))
        assertFalse(ReminderLogic.isPeriodReminderDue(2))
        assertFalse(ReminderLogic.isPeriodReminderDue(null))
    }

    @Test
    fun waterNotBehindWhenGoalAlreadyMet() {
        assertFalse(ReminderLogic.isWaterBehindPace(8, 8, LocalTime(15, 0)))
    }

    @Test
    fun waterNotEvaluatedOutsideWakingHours() {
        assertFalse(ReminderLogic.isWaterBehindPace(0, 8, LocalTime(7, 0)))
        assertFalse(ReminderLogic.isWaterBehindPace(0, 8, LocalTime(22, 0)))
    }

    @Test
    fun waterBehindPaceAtMiddayWithNothingDrunk() {
        // Midday = halfway through 9-21 window; expected ~4 of 8, drunk 0 -> behind.
        assertTrue(ReminderLogic.isWaterBehindPace(0, 8, LocalTime(15, 0)))
    }

    @Test
    fun waterOnPaceIsNotFlagged() {
        // Halfway through window, drunk 5 of 8 (expected ~4) -> ahead, not behind.
        assertFalse(ReminderLogic.isWaterBehindPace(5, 8, LocalTime(15, 0)))
    }

    @Test
    fun waterGoalOfZeroNeverBehind() {
        assertFalse(ReminderLogic.isWaterBehindPace(0, 0, LocalTime(15, 0)))
    }

    @Test
    fun currentPillSlotPicksLatestPassedSlot() {
        assertNull(ReminderLogic.currentPillSlot(LocalTime(8, 0)))
        assertEquals(TimeOfDay.MORNING, ReminderLogic.currentPillSlot(LocalTime(9, 0)))
        assertEquals(TimeOfDay.AFTERNOON, ReminderLogic.currentPillSlot(LocalTime(15, 0)))
        assertEquals(TimeOfDay.EVENING, ReminderLogic.currentPillSlot(LocalTime(21, 0)))
    }
}
