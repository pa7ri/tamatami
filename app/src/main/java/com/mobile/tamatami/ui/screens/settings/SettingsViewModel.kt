package com.mobile.tamatami.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

/**
 * Settings is profile-editing — every field is round-tripped to the
 * [UserProfileEntity] singleton row. We keep an in-memory draft separate from
 * the persisted state so the user can edit freely; "Save" writes the whole
 * row back. The Compose layer collects [state] and [isDirty] and pushes
 * individual field updates via the `set*` methods.
 */
class SettingsViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    /**
     * Original profile (as last loaded from the DB). Used to compute the
     * "dirty" flag and to preserve fields the UI doesn't touch (e.g.
     * `createdAt`, `onboardingComplete`) on save.
     */
    private val original = MutableStateFlow<UserProfileEntity?>(null)
    private val draft = MutableStateFlow(SettingsUiState.Empty)

    val state: StateFlow<SettingsUiState> = draft.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState.Empty,
    )

    /**
     * True when the user has changed at least one field since the last load.
     * Exposed as a [StateFlow] (not a plain `get()` property) so Compose
     * recomposes the Save button when the dirty status flips.
     */
    val isDirty: StateFlow<Boolean> = combine(original, draft) { o, d ->
        if (o == null || !d.loaded) false else o.differsFrom(d)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    init {
        viewModelScope.launch {
            // One-shot load: settings only needs the current snapshot. We don't
            // observe — external writes during the edit session would clobber
            // the user's in-flight edits.
            val profile = userRepository.getProfile()
            if (profile != null) {
                original.update { profile }
                draft.update {
                    SettingsUiState(
                        loaded = true,
                        tamaName = profile.tamaName,
                        lastPeriodStart = profile.lastPeriodStart,
                        avgCycleLengthDays = profile.avgCycleLengthDays,
                        avgPeriodLengthDays = profile.avgPeriodLengthDays,
                        tryingToConceive = profile.tryingToConceive,
                        onContraception = profile.onContraception,
                        irregularCycles = profile.irregularCycles,
                        dailyStepsGoal = profile.dailyStepsGoal,
                        sleepGoalHours = (profile.sleepGoalMinutes / 60).coerceAtLeast(1),
                        waterGoalGlasses = profile.waterGoalGlasses,
                        remindPeriodEnabled = profile.remindPeriodEnabled,
                        remindWaterEnabled = profile.remindWaterEnabled,
                        remindPillsEnabled = profile.remindPillsEnabled,
                    )
                }
            } else {
                // No profile yet (onboarding not complete) — mark loaded so
                // the UI moves past its spinner. The screen still renders
                // editable defaults; user can save to create the row.
                draft.update { it.copy(loaded = true) }
            }
        }
    }

    fun setTamaName(name: String) = draft.update { it.copy(tamaName = name) }
    fun setLastPeriod(date: LocalDate) = draft.update { it.copy(lastPeriodStart = date) }
    fun setCycleLength(days: Int) = draft.update { it.copy(avgCycleLengthDays = days) }
    fun setPeriodLength(days: Int) = draft.update { it.copy(avgPeriodLengthDays = days) }
    fun setTryingToConceive(value: Boolean) = draft.update { it.copy(tryingToConceive = value) }
    fun setOnContraception(value: Boolean) = draft.update { it.copy(onContraception = value) }
    fun setIrregularCycles(value: Boolean) = draft.update { it.copy(irregularCycles = value) }
    fun setStepsGoal(steps: Int) = draft.update { it.copy(dailyStepsGoal = steps) }
    fun setSleepGoalHours(hours: Int) = draft.update { it.copy(sleepGoalHours = hours) }
    fun setWaterGoal(glasses: Int) = draft.update { it.copy(waterGoalGlasses = glasses) }
    fun setRemindPeriod(value: Boolean) = draft.update { it.copy(remindPeriodEnabled = value) }
    fun setRemindWater(value: Boolean) = draft.update { it.copy(remindWaterEnabled = value) }
    fun setRemindPills(value: Boolean) = draft.update { it.copy(remindPillsEnabled = value) }

    fun save(onSaved: () -> Unit = {}) {
        val d = draft.value
        if (!d.loaded) return
        val o = original.value
        val saved = UserProfileEntity(
            id = 0,
            tamaName = d.tamaName.ifBlank { "Tama" },
            lastPeriodStart = d.lastPeriodStart,
            avgCycleLengthDays = d.avgCycleLengthDays,
            avgPeriodLengthDays = d.avgPeriodLengthDays,
            tryingToConceive = d.tryingToConceive,
            onContraception = d.onContraception,
            irregularCycles = d.irregularCycles,
            // Preserve onboarding flag / createdAt from the loaded row.
            onboardingComplete = o?.onboardingComplete ?: true,
            createdAt = o?.createdAt ?: Instant.now(),
            dailyStepsGoal = d.dailyStepsGoal,
            sleepGoalMinutes = d.sleepGoalHours * 60,
            waterGoalGlasses = d.waterGoalGlasses,
            remindPeriodEnabled = d.remindPeriodEnabled,
            remindWaterEnabled = d.remindWaterEnabled,
            remindPillsEnabled = d.remindPillsEnabled,
            // Not surfaced in the UI — preserve whatever was persisted.
            waterReminderIntervalHours = o?.waterReminderIntervalHours ?: 3,
        )
        viewModelScope.launch {
            userRepository.saveProfile(saved)
            // Update original so isDirty flips back to false.
            original.update { saved }
            onSaved()
        }
    }

    class Factory(
        private val userRepository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(userRepository) as T
    }
}

/** Field-by-field comparison between the persisted row and the in-memory draft. */
private fun UserProfileEntity.differsFrom(d: SettingsUiState): Boolean =
    tamaName != d.tamaName ||
        lastPeriodStart != d.lastPeriodStart ||
        avgCycleLengthDays != d.avgCycleLengthDays ||
        avgPeriodLengthDays != d.avgPeriodLengthDays ||
        tryingToConceive != d.tryingToConceive ||
        onContraception != d.onContraception ||
        irregularCycles != d.irregularCycles ||
        dailyStepsGoal != d.dailyStepsGoal ||
        sleepGoalMinutes != d.sleepGoalHours * 60 ||
        waterGoalGlasses != d.waterGoalGlasses ||
        remindPeriodEnabled != d.remindPeriodEnabled ||
        remindWaterEnabled != d.remindWaterEnabled ||
        remindPillsEnabled != d.remindPillsEnabled
