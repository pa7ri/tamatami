package com.mobile.tamatami.util

import java.time.Instant
import java.time.LocalDate

/**
 * Test-friendly clock. Production code uses [SystemClock]; tests can pass a
 * fake without touching the JVM-wide clock.
 */
interface Clock {
    fun today(): LocalDate

    /** Current wall-clock instant; used e.g. for Health Connect day ranges. */
    fun now(): Instant
}

object SystemClock : Clock {
    override fun today(): LocalDate = LocalDate.now()
    override fun now(): Instant = Instant.now()
}
