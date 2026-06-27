package com.mobile.tamatami.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.WorkoutRepository
import com.mobile.tamatami.domain.training.TrainingRecommender
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrainingViewModel(
    private val cycleRepository: CycleRepository,
    private val dailyRepository: DailyLogRepository,
    private val workoutRepository: WorkoutRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    val state: StateFlow<TrainingUiState> = combine(
        cycleRepository.observeTodayCycle(today),
        dailyRepository.observeToday(today),
        workoutRepository.observeRecent(limit = 30),
    ) { cycle, daily, workouts ->
        TrainingUiState(
            recommendation = TrainingRecommender.recommend(cycle.phase, daily.energy),
            recentWorkouts = workouts,
            energy = daily.energy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingUiState.Empty,
    )

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

    class Factory(
        private val cycleRepository: CycleRepository,
        private val dailyRepository: DailyLogRepository,
        private val workoutRepository: WorkoutRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TrainingViewModel(cycleRepository, dailyRepository, workoutRepository, clock) as T
    }
}
