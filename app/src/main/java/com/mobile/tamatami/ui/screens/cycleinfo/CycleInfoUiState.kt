package com.mobile.tamatami.ui.screens.cycleinfo

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot

data class CycleInfoUiState(
    val currentPhase: CyclePhase,
    val selectedPhase: CyclePhase,
    val cycle: CycleSnapshot,
) {
    companion object {
        val Empty = CycleInfoUiState(
            currentPhase = CyclePhase.UNKNOWN,
            selectedPhase = CyclePhase.MENSTRUAL,
            cycle = CycleSnapshot(
                phase = CyclePhase.UNKNOWN,
                cycleDay = 0,
                dayInPhase = 0,
                predictedNextPeriod = null,
                daysUntilNextPeriod = null,
                cycleLength = 28,
            ),
        )
    }
}
