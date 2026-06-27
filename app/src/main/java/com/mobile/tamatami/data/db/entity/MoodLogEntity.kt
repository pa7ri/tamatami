package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.model.Mood
import java.time.LocalDate

@Entity(
    tableName = "mood_log",
    indices = [Index(value = ["date"], unique = true)],
)
data class MoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mood: Mood,
    val energy: Int,
    val notes: String?,
)
