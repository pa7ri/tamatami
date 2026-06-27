package com.mobile.tamatami.domain.model

import java.time.LocalDate

/** Everything the user has logged for a single date. */
data class DailySnapshot(
    val date: LocalDate,
    val waterGlasses: Int,
    val waterGoal: Int,
    val mood: Mood?,
    val energy: Int?,
    val periodFlow: PeriodFlow?,
)
