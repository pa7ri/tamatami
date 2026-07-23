package com.mobile.tamatami.domain.medication

import java.time.LocalTime

/**
 * The three coarse times of day a medication dose can be scheduled for. Kept
 * coarse (rather than exact clock times) to match the app's lightweight logging
 * feel; reminders fire at fixed default clock times per slot ([defaultTime]).
 */
enum class TimeOfDay(val displayName: String, val defaultTime: LocalTime) {
    MORNING("Morning", LocalTime.of(9, 0)),
    AFTERNOON("Afternoon", LocalTime.of(14, 0)),
    EVENING("Evening", LocalTime.of(20, 0)),
}
