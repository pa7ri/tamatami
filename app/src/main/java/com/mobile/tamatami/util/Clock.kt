package com.mobile.tamatami.util

import kotlinx.datetime.Clock as KtxClock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Test-friendly clock. Production code uses [SystemClock]; tests can pass a
 * fake without touching the JVM-wide clock. Now speaks kotlinx-datetime types
 * to match the shared (KMP) data layer.
 */
interface Clock {
    fun today(): LocalDate

    /** Current wall-clock instant; used e.g. for Health Connect day ranges. */
    fun now(): Instant
}

object SystemClock : Clock {
    override fun today(): LocalDate = KtxClock.System.todayIn(TimeZone.currentSystemDefault())
    override fun now(): Instant = KtxClock.System.now()
}
