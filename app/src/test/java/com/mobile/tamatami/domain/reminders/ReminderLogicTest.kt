package com.mobile.tamatami.domain.reminders

import com.google.common.truth.Truth.assertThat
import com.mobile.tamatami.domain.medication.TimeOfDay
import org.junit.Test
import java.time.LocalTime

class ReminderLogicTest {

    @Test
    fun `period reminder fires only the day before`() {
        assertThat(ReminderLogic.isPeriodReminderDue(1)).isTrue()
        assertThat(ReminderLogic.isPeriodReminderDue(0)).isFalse()
        assertThat(ReminderLogic.isPeriodReminderDue(2)).isFalse()
        assertThat(ReminderLogic.isPeriodReminderDue(null)).isFalse()
    }

    @Test
    fun `water not behind when goal already met`() {
        assertThat(ReminderLogic.isWaterBehindPace(8, 8, LocalTime.of(15, 0))).isFalse()
    }

    @Test
    fun `water not evaluated outside waking hours`() {
        assertThat(ReminderLogic.isWaterBehindPace(0, 8, LocalTime.of(7, 0))).isFalse()
        assertThat(ReminderLogic.isWaterBehindPace(0, 8, LocalTime.of(22, 0))).isFalse()
    }

    @Test
    fun `water behind pace at midday with nothing drunk`() {
        // Midday = halfway through 9-21 window; expected ~4 of 8, drunk 0 -> behind.
        assertThat(ReminderLogic.isWaterBehindPace(0, 8, LocalTime.of(15, 0))).isTrue()
    }

    @Test
    fun `water on pace is not flagged`() {
        // Halfway through window, drunk 5 of 8 (expected ~4) -> ahead, not behind.
        assertThat(ReminderLogic.isWaterBehindPace(5, 8, LocalTime.of(15, 0))).isFalse()
    }

    @Test
    fun `water goal of zero never behind`() {
        assertThat(ReminderLogic.isWaterBehindPace(0, 0, LocalTime.of(15, 0))).isFalse()
    }

    @Test
    fun `current pill slot picks latest passed slot`() {
        assertThat(ReminderLogic.currentPillSlot(LocalTime.of(8, 0))).isNull()
        assertThat(ReminderLogic.currentPillSlot(LocalTime.of(9, 0))).isEqualTo(TimeOfDay.MORNING)
        assertThat(ReminderLogic.currentPillSlot(LocalTime.of(15, 0))).isEqualTo(TimeOfDay.AFTERNOON)
        assertThat(ReminderLogic.currentPillSlot(LocalTime.of(21, 0))).isEqualTo(TimeOfDay.EVENING)
    }
}
