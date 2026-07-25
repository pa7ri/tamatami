package com.mobile.tamatami.domain.medication

import kotlinx.datetime.LocalTime

/**
 * The three coarse times of day a medication dose can be scheduled for. Kept
 * coarse (rather than exact clock times) to match the app's lightweight logging
 * feel; reminders fire at fixed default clock times per slot ([defaultTime]).
 */
enum class TimeOfDay(val displayName: String, val defaultTime: LocalTime) {
    MORNING("Morning", LocalTime(9, 0)),
    AFTERNOON("Afternoon", LocalTime(14, 0)),
    EVENING("Evening", LocalTime(20, 0)),
}
