package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "water_log",
    indices = [Index(value = ["date"], unique = true)],
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val glasses: Int,
    val goal: Int = 8,
)
