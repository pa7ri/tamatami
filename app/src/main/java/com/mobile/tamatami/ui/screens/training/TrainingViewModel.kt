package com.mobile.tamatami.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.health.StepDataSource
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.data.repository.WorkoutRepository
import com.mobile.tamatami.domain.sleep.ExpectedSleepPredictor
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.training.TrainingRecommender
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

class TrainingViewModel(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val dailyRepository: DailyLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val stepDataSource: StepDataSource,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    // Steps are fetched live from Health Connect (not persisted), so they live
    // in their own flow rather than the daily-snapshot combine.
    private val stepsState = MutableStateFlow<StepsUiState>(StepsUiState.Loading)

    init {
        refreshSteps()
    }

    val state: StateFlow<TrainingUiState> = combine(
        cycleRepository.observeTodayCycle(today),
        dailyRepository.observeToday(today),
        workoutRepository.observeRecent(limit = 30),
        stepsState,
        userRepository.observeProfile(),
    ) { cycle, daily, workouts, steps, profile ->
        // Partial state; recent-sleep prediction folded in by the next combine.
        TrainingUiState(
            recommendation = TrainingRecommender.recommend(cycle.phase, daily.energy),
            recentWorkouts = workouts,
            energy = daily.energy,
            sleep = daily.sleep,
            steps = steps,
            stepsGoal = profile?.dailyStepsGoal ?: 8_000,
            sleepGoalMinutes = profile?.sleepGoalMinutes ?: (8 * 60),
            expectedSleep = null,
        )
    }.combine(dailyRepository.observeRecentSleep(14)) { partial, recent ->
        partial.copy(
            expectedSleep = ExpectedSleepPredictor.predict(recent.map { it.quality }),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState.Empty,
    )

    /** Re-evaluate Health Connect availability/permission and (if granted) today's steps. */
    fun refreshSteps() {
        viewModelScope.launch {
            val availability = stepDataSource.availability()
            if (availability != StepDataSource.Availability.AVAILABLE) {
                stepsState.value = StepsUiState.Unavailable(availability)
                return@launch
            }
            if (!stepDataSource.hasPermission()) {
                stepsState.value = StepsUiState.NeedsPermission
                return@launch
            }
            stepsState.value = StepsUiState.Ready(count = null)
            val count = runCatching { stepDataSource.stepsFor(today.toJavaLocalDate()) }.getOrNull()
            stepsState.value = StepsUiState.Ready(count = count)
        }
    }

    /** The permission set + contract for the Training screen's launcher. */
    val stepsPermissions: Set<String> get() = stepDataSource.permissions
    val stepsPermissionContract get() = stepDataSource.permissionContract

    fun logWorkout(
        date: LocalDate,
        type: WorkoutType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        notes: String?,
    ) {
        viewModelScope.launch {
            workoutRepository.logWorkout(date, type, durationMinutes, intensity, notes)
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch { workoutRepository.delete(id) }
    }

    fun logSleep(bedMinuteOfDay: Int, wakeMinuteOfDay: Int, rating: SleepRating) {
        viewModelScope.launch {
            dailyRepository.logSleep(today, bedMinuteOfDay, wakeMinuteOfDay, rating)
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val cycleRepository: CycleRepository,
        private val dailyRepository: DailyLogRepository,
        private val workoutRepository: WorkoutRepository,
        private val stepDataSource: StepDataSource,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TrainingViewModel(
                userRepository, cycleRepository, dailyRepository, workoutRepository,
                stepDataSource, clock,
            ) as T
    }
}
