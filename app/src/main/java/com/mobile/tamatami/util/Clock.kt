package com.mobile.tamatami.util

import java.time.LocalDate

/**
 * Test-friendly clock. Production code uses [SystemClock]; tests can pass a
 * [FakeClock] without touching the JVM-wide clock.
 */
interface Clock {
    fun today(): LocalDate
}

object SystemClock : Clock {
    override fun today(): LocalDate = LocalDate.now()
}
