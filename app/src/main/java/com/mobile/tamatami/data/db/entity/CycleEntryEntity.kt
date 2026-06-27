package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "cycle_entry",
    indices = [Index(value = ["startDate"], unique = true)],
)
data class CycleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val lengthDays: Int?,
)
