package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.WorkoutLogDao
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.model.Workout
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class WorkoutRepository(
    private val dao: WorkoutLogDao,
) {
    fun observeRecent(limit: Int = 30): Flow<List<Workout>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toWorkout() } }

    fun observeByDate(date: LocalDate): Flow<List<Workout>> =
        dao.observeByDate(date).map { rows -> rows.map { it.toWorkout() } }

    suspend fun logWorkout(
        date: LocalDate,
        type: WorkoutType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        notes: String? = null,
        id: Long = 0,
    ) {
        dao.upsert(
            WorkoutLogEntity(
                id = id,
                date = date,
                type = type,
                durationMinutes = durationMinutes.coerceAtLeast(1),
                intensity = intensity,
                notes = notes,
            )
        )
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
