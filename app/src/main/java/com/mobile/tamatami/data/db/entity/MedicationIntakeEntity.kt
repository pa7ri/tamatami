package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.medication.TimeOfDay
import java.time.LocalDate

/**
 * One logged "taken" dose: a [medicationId] + [date] + [slot]. The unique index
 * makes a given slot tappable-once-per-day (mirrors [SleepLogEntity]'s per-date
 * uniqueness); "un-taking" a slot deletes the row.
 */
@Entity(
    tableName = "medication_intake",
    indices = [Index(value = ["medicationId", "date", "slot"], unique = true)],
)
data class MedicationIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val date: LocalDate,
    val slot: TimeOfDay,
)
