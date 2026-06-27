package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import java.time.LocalDate

@Entity(
    tableName = "workout_log",
    indices = [Index(value = ["date"])],
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: WorkoutType,
    val durationMinutes: Int,
    val intensity: WorkoutIntensity,
    val notes: String?,
)
