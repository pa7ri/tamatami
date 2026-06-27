package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.WorkoutLogDao
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WorkoutRepository(
    private val dao: WorkoutLogDao,
) {
    fun observeRecent(limit: Int = 30): Flow<List<WorkoutLogEntity>> = dao.observeRecent(limit)

    fun observeByDate(date: LocalDate): Flow<List<WorkoutLogEntity>> = dao.observeByDate(date)

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
