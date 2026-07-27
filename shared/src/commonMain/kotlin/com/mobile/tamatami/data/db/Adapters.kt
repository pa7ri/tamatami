package com.mobile.tamatami.data.db

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/** Int stored as SQLite INTEGER (Long). */
internal val intAdapter = object : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}

/** Long PK stored as INTEGER — identity, but SQLDelight still requires an adapter slot. */
internal val longAdapter = object : ColumnAdapter<Long, Long> {
    override fun decode(databaseValue: Long): Long = databaseValue
    override fun encode(value: Long): Long = value
}

/** Float stored as SQLite REAL (Double). */
internal val floatAdapter = object : ColumnAdapter<Float, Double> {
    override fun decode(databaseValue: Double): Float = databaseValue.toFloat()
    override fun encode(value: Float): Double = value.toDouble()
}

/** LocalDate stored as epoch-day INTEGER. */
internal val localDateAdapter = object : ColumnAdapter<LocalDate, Long> {
    override fun decode(databaseValue: Long): LocalDate = LocalDate.fromEpochDays(databaseValue.toInt())
    override fun encode(value: LocalDate): Long = value.toEpochDays().toLong()
}

/** Instant stored as epoch-milli INTEGER. */
internal val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

/** Generic enum-by-name adapter (stored as TEXT). */
internal inline fun <reified T : Enum<T>> enumAdapter(): ColumnAdapter<T, String> =
    object : ColumnAdapter<T, String> {
        override fun decode(databaseValue: String): T = enumValueOf<T>(databaseValue)
        override fun encode(value: T): String = value.name
    }
