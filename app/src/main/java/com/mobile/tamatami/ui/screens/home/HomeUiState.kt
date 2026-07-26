package com.mobile.tamatami.ui.screens.home

import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.TamagotchiState
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

data class HomeUiState(
    val tamaName: String,
    val cycle: CycleSnapshot,
    val daily: DailySnapshot,
    val tama: TamagotchiState,
    /** App-wide water goal from the profile (overrides the per-row default). */
    val waterGoal: Int = 8,
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
            daily = DailySnapshot.empty(LocalDate.now().toKotlinLocalDate()),
            tama = TamagotchiState.Idle,
            waterGoal = 8,
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
