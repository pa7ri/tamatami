package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.model.PeriodFlow
import java.time.LocalDate

@Entity(
    tableName = "period_day",
    indices = [Index(value = ["date"], unique = true)],
)
data class PeriodDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val flow: PeriodFlow,
)
