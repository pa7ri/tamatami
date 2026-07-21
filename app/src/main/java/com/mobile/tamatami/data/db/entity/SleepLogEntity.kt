package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.sleep.SleepRating
import java.time.LocalDate

/**
 * One night's sleep, keyed by the [date] it's logged against (the wake-up day).
 * Bed/wake are stored as minute-of-day (0..1439); [durationMinutes] is
 * precomputed at write time and already accounts for crossing midnight.
 * Estimated quality is derived on read, not stored.
 */
@Entity(
    tableName = "sleep_log",
    indices = [Index(value = ["date"], unique = true)],
)
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val bedMinuteOfDay: Int,
    val wakeMinuteOfDay: Int,
    val durationMinutes: Int,
    val rating: SleepRating,
)
