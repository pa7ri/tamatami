package com.mobile.tamatami.data.repository

import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.TamagotchiState
import com.mobile.tamatami.domain.tamagotchi.TamagotchiMoodEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

class TamagotchiRepository(
    private val cycleRepo: CycleRepository,
    private val dailyRepo: DailyLogRepository,
) {
    fun observe(today: java.time.LocalDate): Flow<TamagotchiState> = combine(
        cycleRepo.observeTodayCycle(today),
        dailyRepo.observeToday(today),
    ) { cycle: CycleSnapshot, daily: DailySnapshot ->
        TamagotchiMoodEngine.derive(cycle, daily)
    }.distinctUntilChanged()
}
