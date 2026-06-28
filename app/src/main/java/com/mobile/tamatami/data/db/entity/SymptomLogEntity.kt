package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import com.mobile.tamatami.domain.model.Symptom
import java.time.LocalDate

/**
 * Bridge row — one record per (date, symptom). Composite primary key gives
 * us an implicit unique constraint, so toggling the same symptom twice in a
 * day collapses to a single row.
 */
@Entity(
    tableName = "symptom_log",
    primaryKeys = ["date", "symptom"],
    indices = [Index("date")],
)
data class SymptomLogEntity(
    val date: LocalDate,
    val symptom: Symptom,
)
