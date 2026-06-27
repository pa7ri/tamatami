package com.mobile.tamatami.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class NutritionViewModel(
    cycleRepository: CycleRepository,
    clock: Clock,
) : ViewModel() {

    private val selectedCraving = MutableStateFlow<CravingHint?>(null)

    val state: StateFlow<NutritionUiState> = combine(
        cycleRepository.observeTodayCycle(clock.today()),
        selectedCraving,
    ) { cycle, craving ->
        NutritionUiState(currentPhase = cycle.phase, selectedCraving = craving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionUiState.Empty,
    )

    fun toggleCraving(craving: CravingHint) = selectedCraving.update {
        if (it == craving) null else craving
    }

    class Factory(
        private val cycleRepository: CycleRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NutritionViewModel(cycleRepository, clock) as T
    }
}
