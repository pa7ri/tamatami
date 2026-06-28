package com.mobile.tamatami.ui.screens.calendar

import com.mobile.tamatami.domain.calendar.CalendarDay
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val displayedMonth: YearMonth,
    val days: List<CalendarDay>,
    val selectedDate: LocalDate?,
    val cycle: CycleSnapshot,
    /** Daily log for whichever date the card is showing (selected or today). */
    val selectedDaySnapshot: DailySnapshot?,
) {
    companion object {
        fun empty(today: LocalDate): CalendarUiState = CalendarUiState(
            displayedMonth = YearMonth.from(today),
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
