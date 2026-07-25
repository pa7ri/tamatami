package com.mobile.tamatami.ui.screens.settings

import java.time.LocalDate

/**
 * UI projection of [com.mobile.tamatami.data.db.entity.UserProfileEntity].
 *
 * `loaded = false` means we're still waiting for the first DB emission — the
 * screen shows a spinner in that case. Once loaded, the fields mirror the
 * persisted profile and edits flow back through the ViewModel.
 */
data class SettingsUiState(
    val loaded: Boolean = false,
    val tamaName: String = "",
    val lastPeriodStart: LocalDate = LocalDate.now(),
    val avgCycleLengthDays: Int = 28,
    val avgPeriodLengthDays: Int = 5,
    val tryingToConceive: Boolean = false,
    val onContraception: Boolean = false,
    val irregularCycles: Boolean = false,
    val dailyStepsGoal: Int = 8_000,
    val sleepGoalHours: Int = 8,
    val waterGoalGlasses: Int = 8,
    // Per-type reminder toggles. Defaults mirror UserProfileEntity.
    val remindPeriodEnabled: Boolean = true,
    val remindWaterEnabled: Boolean = false,
    val remindPillsEnabled: Boolean = true,
) {
    companion object {
        val Empty = SettingsUiState()
    }
}
