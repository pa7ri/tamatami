package com.mobile.tamatami.ui.screens.home

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.TamagotchiState
import java.time.LocalDate

data class HomeUiState(
    val tamaName: String,
    val cycle: CycleSnapshot,
    val daily: DailySnapshot,
    val tama: TamagotchiState,
) {
    companion object {
        val Empty = HomeUiState(
            tamaName = "Tama",
            cycle = CycleSnapshot(
                phase = CyclePhase.UNKNOWN,
                cycleDay = 0,
                dayInPhase = 0,
                predictedNextPeriod = null,
                daysUntilNextPeriod = null,
                cycleLength = 28,
            ),
            daily = DailySnapshot.empty(LocalDate.now()),
            tama = TamagotchiState.Idle,
        )
    }
}

fun CyclePhase.displayName(): String = when (this) {
    CyclePhase.MENSTRUAL -> "Menstrual"
    CyclePhase.FOLLICULAR -> "Follicular"
    CyclePhase.OVULATORY -> "Ovulatory"
    CyclePhase.LUTEAL -> "Luteal"
    CyclePhase.UNKNOWN -> "—"
}
