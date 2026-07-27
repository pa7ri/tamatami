package com.mobile.tamatami.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.domain.calendar.MonthBuilder
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    // Displayed month = first day of that month (kotlinx has no YearMonth).
    private val displayedMonth = MutableStateFlow(today.let { LocalDate(it.year, it.monthNumber, 1) })
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    /**
     * The state stream. The 5-arg [combine] handles the calendar-grid inputs;
     * a separate [flatMapLatest] on [selectedDate] keeps the daily snapshot
     * flowing for whichever date the day card is currently showing (selected,
     * or today when nothing is selected).
     */
    val state: StateFlow<CalendarUiState> = combine(
        userRepository.observeProfile(),
        cycleRepository.observeRecentPeriodDays(),
        cycleRepository.observeTodayCycle(today),
        displayedMonth,
        selectedDate,
    ) { profile, periodDays, cycle, month, selected ->
        CalendarUiState(
            displayedMonth = month,
            days = MonthBuilder.build(
                year = month.year,
                monthNumber = month.monthNumber,
                profile = profile?.let {
                    CycleProfile(
                        lastPeriodStart = it.lastPeriodStart,
                        avgCycleLengthDays = it.avgCycleLengthDays,
                        avgPeriodLengthDays = it.avgPeriodLengthDays,
                    )
                },
                loggedPeriodDays = periodDays,
                today = today,
            ),
            selectedDate = selected,
            cycle = cycle,
            selectedDaySnapshot = null, // filled in by the next combine
        )
    }.flatMapLatest { partial ->
        val target = partial.selectedDate ?: today
        dailyLogRepository.observeToday(target).map { snapshot ->
            partial.copy(selectedDaySnapshot = snapshot)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState.empty(today),
    )

    fun goPrevMonth() = displayedMonth.update { it.minus(1, DateTimeUnit.MONTH) }
    fun goNextMonth() = displayedMonth.update { it.plus(1, DateTimeUnit.MONTH) }
    fun selectDate(date: LocalDate?) = selectedDate.update { date }

    fun setFlow(date: LocalDate, flow: PeriodFlow) {
        viewModelScope.launch { cycleRepository.logPeriodDay(date, flow) }
    }

    fun setMood(date: LocalDate, mood: Mood) {
        viewModelScope.launch { dailyLogRepository.setMood(date, mood) }
    }

    fun setEnergy(date: LocalDate, energy: Int) {
        viewModelScope.launch { dailyLogRepository.setEnergy(date, energy) }
    }

    fun toggleSymptom(date: LocalDate, symptom: Symptom) {
        viewModelScope.launch { dailyLogRepository.toggleSymptom(date, symptom) }
    }

    fun setWater(date: LocalDate, glasses: Int) {
        viewModelScope.launch { dailyLogRepository.setWater(date, glasses) }
    }

    fun setCraving(date: LocalDate, craving: CravingHint?) {
        viewModelScope.launch { dailyLogRepository.setCraving(date, craving) }
    }

    fun logWorkout(
        date: LocalDate,
        type: WorkoutType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        notes: String?,
    ) {
        viewModelScope.launch {
            dailyLogRepository.logWorkout(date, type, durationMinutes, intensity, notes)
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val cycleRepository: CycleRepository,
        private val dailyLogRepository: DailyLogRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(
            userRepository, cycleRepository, dailyLogRepository, clock,
        ) as T
    }
}
