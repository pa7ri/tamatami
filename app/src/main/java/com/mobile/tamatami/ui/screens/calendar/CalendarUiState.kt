package com.mobile.tamatami.ui.screens.calendar

import com.mobile.tamatami.domain.calendar.CalendarDay
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    /** First day of the displayed month (kotlinx-datetime has no YearMonth). */
    val displayedMonth: LocalDate,
    val days: List<CalendarDay>,
    val selectedDate: LocalDate?,
    val cycle: CycleSnapshot,
    /** Daily log for whichever date the card is showing (selected or today). */
    val selectedDaySnapshot: DailySnapshot?,
) {
    companion object {
        fun empty(today: LocalDate): CalendarUiState = CalendarUiState(
            displayedMonth = LocalDate(today.year, today.monthNumber, 1),
            days = emptyList(),
            selectedDate = null,
            cycle = CycleSnapshot(
                phase = CyclePhase.UNKNOWN,
                cycleDay = 0,
                dayInPhase = 0,
                predictedNextPeriod = null,
                daysUntilNextPeriod = null,
                cycleLength = 28,
            ),
            selectedDaySnapshot = null,
        )
    }
}
