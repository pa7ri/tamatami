package com.mobile.tamatami.ui.screens.cycleinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CycleInfoViewModel(
    cycleRepository: CycleRepository,
    clock: Clock,
) : ViewModel() {

    private val selectedPhase = MutableStateFlow<CyclePhase?>(null)

    val state: StateFlow<CycleInfoUiState> = combine(
        cycleRepository.observeTodayCycle(clock.today()),
        selectedPhase,
    ) { cycle, selected ->
        val resolvedSelected = selected
            ?: cycle.phase.takeIf { it != CyclePhase.UNKNOWN }
            ?: CyclePhase.MENSTRUAL
        CycleInfoUiState(
            currentPhase = cycle.phase,
            selectedPhase = resolvedSelected,
            cycle = cycle,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CycleInfoUiState.Empty,
    )

    fun selectPhase(phase: CyclePhase) = selectedPhase.update { phase }

    class Factory(
        private val cycleRepository: CycleRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CycleInfoViewModel(cycleRepository, clock) as T
    }
}
