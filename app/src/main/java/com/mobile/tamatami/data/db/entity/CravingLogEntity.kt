package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.nutrition.CravingHint
import java.time.LocalDate

/** One craving logged per date. Unique index on `date` enforces single-value. */
@Entity(
    tableName = "craving_log",
    indices = [Index(value = ["date"], unique = true)],
)
data class CravingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val craving: CravingHint,
)
