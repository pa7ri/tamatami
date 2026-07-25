package com.mobile.tamatami.ui.screens.pills

import com.mobile.tamatami.data.repository.MedicationToday

/**
 * Today's medications, each joined with the slots already logged as taken.
 * [loaded] gates the initial spinner (mirrors [SettingsUiState.loaded]) so an
 * empty [meds] list reads as "nothing tracked yet" rather than "still loading".
 */
data class PillsUiState(
    val loaded: Boolean,
    val meds: List<MedicationToday>,
) {
    companion object {
        val Empty = PillsUiState(loaded = false, meds = emptyList())
    }
}
