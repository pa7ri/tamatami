package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "hormone_log",
    indices = [Index(value = ["date"]), Index(value = ["hormone"])],
)
data class HormoneLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val hormone: String,
    val value: Float,
    val unit: String,
    val notes: String? = null,
)
