package com.mobile.tamatami.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.TamagotchiRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val dailyRepository: DailyLogRepository,
    private val tamaRepository: TamagotchiRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    val state: StateFlow<HomeUiState> = combine(
        userRepository.observeProfile(),
        cycleRepository.observeTodayCycle(today),
        dailyRepository.observeToday(today),
        tamaRepository.observe(today),
    ) { profile, cycle, daily, tama ->
        HomeUiState(
            tamaName = profile?.tamaName ?: "Tama",
            cycle = cycle,
            daily = daily,
            tama = tama,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Empty,
    )

    fun addWater() {
        viewModelScope.launch { dailyRepository.incrementWater(today) }
    }

    fun removeWater() {
        viewModelScope.launch {
            val current = state.value.daily.waterGlasses
            dailyRepository.setWater(today, (current - 1).coerceAtLeast(0))
        }
    }

    fun setMood(mood: Mood) {
        viewModelScope.launch { dailyRepository.setMood(today, mood) }
    }

    fun setFlow(flow: PeriodFlow) {
        viewModelScope.launch { dailyRepository.setFlow(today, flow) }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val cycleRepository: CycleRepository,
        private val dailyRepository: DailyLogRepository,
        private val tamaRepository: TamagotchiRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                userRepository, cycleRepository, dailyRepository, tamaRepository, clock,
            ) as T
    }
}
