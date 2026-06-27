package com.mobile.tamatami.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.domain.calendar.MonthBuilder
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val periodDayDao: PeriodDayDao,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    private val displayedMonth = MutableStateFlow(YearMonth.from(clock.today()))
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<CalendarUiState> = combine(
        userRepository.observeProfile(),
        periodDayDao.observeRecent(),
        cycleRepository.observeTodayCycle(today),
        displayedMonth,
        selectedDate,
    ) { profile, periodDays, cycle, month, selected ->
        CalendarUiState(
            displayedMonth = month,
            days = MonthBuilder.build(month, profile, periodDays, today),
            selectedDate = selected,
            cycle = cycle,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState.empty(today),
    )

    fun goPrevMonth() = displayedMonth.update { it.minusMonths(1) }
    fun goNextMonth() = displayedMonth.update { it.plusMonths(1) }
    fun selectDate(date: LocalDate?) = selectedDate.update { date }

    fun setFlow(date: LocalDate, flow: PeriodFlow) {
        viewModelScope.launch { cycleRepository.logPeriodDay(date, flow) }
    }

    fun setMood(date: LocalDate, mood: Mood, energy: Int) {
        viewModelScope.launch { dailyLogRepository.setMood(date, mood, energy) }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val cycleRepository: CycleRepository,
        private val dailyLogRepository: DailyLogRepository,
        private val periodDayDao: PeriodDayDao,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(
            userRepository, cycleRepository, dailyLogRepository, periodDayDao, clock,
        ) as T
    }
}
