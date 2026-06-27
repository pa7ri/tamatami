package com.mobile.tamatami.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

/** In-progress onboarding answers, persisted in memory only until the user confirms. */
data class OnboardingDraft(
    val tamaName: String = "Tama",
    val lastPeriodStart: LocalDate = LocalDate.now().minusDays(3),
    val avgCycleLengthDays: Int = 28,
    val avgPeriodLengthDays: Int = 5,
    val tryingToConceive: Boolean = false,
    val onContraception: Boolean = false,
    val irregularCycles: Boolean = false,
)

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
) : ViewModel() {

    private val _draft = MutableStateFlow(OnboardingDraft())
    val draft: StateFlow<OnboardingDraft> = _draft.asStateFlow()

    fun setName(name: String) = _draft.update { it.copy(tamaName = name) }
    fun setLastPeriod(date: LocalDate) = _draft.update { it.copy(lastPeriodStart = date) }
    fun setCycleLength(days: Int) = _draft.update { it.copy(avgCycleLengthDays = days) }
    fun setPeriodLength(days: Int) = _draft.update { it.copy(avgPeriodLengthDays = days) }
    fun setTryingToConceive(value: Boolean) = _draft.update { it.copy(tryingToConceive = value) }
    fun setOnContraception(value: Boolean) = _draft.update { it.copy(onContraception = value) }
    fun setIrregularCycles(value: Boolean) = _draft.update { it.copy(irregularCycles = value) }

    fun finish(onDone: () -> Unit) {
        val d = _draft.value
        viewModelScope.launch {
            userRepository.saveProfile(
                UserProfileEntity(
                    id = 0,
                    tamaName = d.tamaName.ifBlank { "Tama" },
                    lastPeriodStart = d.lastPeriodStart,
                    avgCycleLengthDays = d.avgCycleLengthDays,
                    avgPeriodLengthDays = d.avgPeriodLengthDays,
                    tryingToConceive = d.tryingToConceive,
                    onContraception = d.onContraception,
                    irregularCycles = d.irregularCycles,
                    onboardingComplete = true,
                    createdAt = Instant.now(),
                )
            )
            cycleRepository.seedCycleEntry(d.lastPeriodStart, d.avgCycleLengthDays)
            onDone()
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val cycleRepository: CycleRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(userRepository, cycleRepository) as T
        }
    }
}
