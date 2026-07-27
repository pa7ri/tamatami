package com.mobile.tamatami.domain.model

import kotlinx.datetime.LocalDate

/**
 * A single logged period day — the slice of Room's `PeriodDayEntity` the cycle
 * history/prediction math needs, decoupled so it can live in commonMain.
 */
data class PeriodDay(
    val date: LocalDate,
    val flow: PeriodFlow,
)
