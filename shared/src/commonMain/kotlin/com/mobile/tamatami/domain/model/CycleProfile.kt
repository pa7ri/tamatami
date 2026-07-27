package com.mobile.tamatami.domain.model

import kotlinx.datetime.LocalDate

/**
 * The slice of the user's profile the cycle math needs — decoupled from the
 * Room `UserProfileEntity` so the cycle/calendar logic can live in commonMain
 * (and compile for iOS). The data layer maps its entity to this at the boundary.
 */
data class CycleProfile(
    val lastPeriodStart: LocalDate,
    val avgCycleLengthDays: Int,
    val avgPeriodLengthDays: Int,
)
