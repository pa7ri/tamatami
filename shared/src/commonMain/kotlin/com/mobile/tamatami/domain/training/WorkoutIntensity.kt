package com.mobile.tamatami.domain.training

/** Workout effort levels, ordered low→high so we can step up/down. */
enum class WorkoutIntensity {
    LOW,
    MODERATE,
    HIGH,
    ;

    fun downshift(): WorkoutIntensity = when (this) {
        HIGH -> MODERATE
        MODERATE -> LOW
        LOW -> LOW
    }

    fun upshift(): WorkoutIntensity = when (this) {
        LOW -> MODERATE
        MODERATE -> HIGH
        HIGH -> HIGH
    }
}
