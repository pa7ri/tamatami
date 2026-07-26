package com.mobile.tamatami.domain.model

import kotlinx.datetime.LocalDate

/** Pure snapshot describing where the user sits in their cycle today. */
data class CycleSnapshot(
    val phase: CyclePhase,
    val cycleDay: Int,
    val dayInPhase: Int,
    val predictedNextPeriod: LocalDate?,
    val daysUntilNextPeriod: Int?,
    val cycleLength: Int,
)
