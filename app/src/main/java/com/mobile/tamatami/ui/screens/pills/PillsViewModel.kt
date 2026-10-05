package com.mobile.tamatami.ui.screens.pills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.MedicationRepository
import com.mobile.tamatami.domain.medication.MedicationFrequency
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Pills screen: observes today's medications + logged intakes and
 * relays user actions (add / delete / toggle-taken) to [MedicationRepository].
 * Mirrors [com.mobile.tamatami.ui.screens.hormones.HormonesViewModel] — a thin
 * reactive wrapper over the repository, with no logic of its own.
 */
class PillsViewModel(
    private val medicationRepository: MedicationRepository,
    private val clock: Clock,
) : ViewModel() {

    private val today get() = clock.today()

    val state: StateFlow<PillsUiState> =
        medicationRepository.observeToday(today)
            .map { PillsUiState(loaded = true, meds = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PillsUiState.Empty,
            )

    /** Toggle whether [slot] is logged as taken for [medicationId] today. */
    fun toggleTaken(medicationId: Long, slot: TimeOfDay, taken: Boolean) {
        viewModelScope.launch {
            medicationRepository.setTaken(medicationId, today, slot, taken)
        }
    }

    fun addMedication(name: String, slots: Set<TimeOfDay>, frequency: MedicationFrequency) {
        viewModelScope.launch {
            medicationRepository.addMedication(
                name = name,
                dosesPerDay = slots.size,
                slots = slots,
                frequency = frequency,
                now = clock.now(),
            )
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch { medicationRepository.deleteMedication(id) }
    }

    class Factory(
        private val medicationRepository: MedicationRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PillsViewModel(medicationRepository, clock) as T
    }
}
