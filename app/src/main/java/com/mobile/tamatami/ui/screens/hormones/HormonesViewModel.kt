package com.mobile.tamatami.ui.screens.hormones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.repository.HormoneRepository
import com.mobile.tamatami.domain.hormones.HormoneMarker
import com.mobile.tamatami.util.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HormonesViewModel(
    private val hormoneRepository: HormoneRepository,
    private val clock: Clock,
) : ViewModel() {

    private val selectedMarker = MutableStateFlow(HormoneMarker.ESTROGEN)

    private val recentSince: LocalDate get() = clock.today().minusDays(90)
    private val chartSince: LocalDate get() = clock.today().minusDays(30)

    val state: StateFlow<HormonesUiState> = combine(
        selectedMarker,
        hormoneRepository.observeRecent(recentSince),
        selectedMarker.flatMapLatest { marker ->
            hormoneRepository.observeByMarker(marker, chartSince)
        },
    ) { marker, recent, markerEntries ->
        HormonesUiState(
            selectedMarker = marker,
            entries = recent,
            markerPoints = markerEntries
                .sortedBy { it.date }
                .map { it.date to it.value },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HormonesUiState.Empty,
    )

    fun selectMarker(marker: HormoneMarker) = selectedMarker.update { marker }

    fun saveEntry(
        date: LocalDate,
        marker: HormoneMarker,
        value: Float,
        unit: String,
        notes: String?,
    ) {
        viewModelScope.launch {
            hormoneRepository.upsert(
                date = date, marker = marker, value = value, unit = unit, notes = notes,
            )
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { hormoneRepository.delete(id) }
    }

    class Factory(
        private val hormoneRepository: HormoneRepository,
        private val clock: Clock,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HormonesViewModel(hormoneRepository, clock) as T
    }
}
