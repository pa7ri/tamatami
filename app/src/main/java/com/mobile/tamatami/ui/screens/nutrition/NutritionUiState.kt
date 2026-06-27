package com.mobile.tamatami.ui.screens.nutrition

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.nutrition.CravingHint

data class NutritionUiState(
    val currentPhase: CyclePhase,
    val selectedCraving: CravingHint?,
) {
    companion object {
        val Empty = NutritionUiState(
            currentPhase = CyclePhase.UNKNOWN,
            selectedCraving = null,
        )
    }
}
