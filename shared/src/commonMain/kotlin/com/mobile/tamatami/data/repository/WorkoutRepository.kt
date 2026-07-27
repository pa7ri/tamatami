package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.db.WorkoutLog
import com.mobile.tamatami.domain.model.Workout
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Workout logs, backed by SQLDelight. Reactive reads map the generated
 * [WorkoutLog] row to the shared [Workout] domain model.
 */
class WorkoutRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun observeRecent(limit: Int = 30): Flow<List<Workout>> =
        db.workoutLogQueries.observeRecent(limit.toLong()).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toWorkout() } }

    fun observeByDate(date: LocalDate): Flow<List<Workout>> =
        db.workoutLogQueries.observeByDate(date).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toWorkout() } }

    suspend fun logWorkout(
        date: LocalDate,
        type: WorkoutType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        notes: String? = null,
        id: Long = 0,
    ) {
        withContext(dispatcher) {
            if (id == 0L) {
                db.workoutLogQueries.insert(
                    date = date,
                    type = type,
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    intensity = intensity,
                    notes = notes,
                )
            } else {
                db.workoutLogQueries.upsert(
                    id = id,
                    date = date,
                    type = type,
                    durationMinutes = durationMinutes.coerceAtLeast(1),
                    intensity = intensity,
                    notes = notes,
                )
            }
        }
    }

    suspend fun delete(id: Long) {
        withContext(dispatcher) { db.workoutLogQueries.deleteById(id) }
    }
}

/** Map the generated workout row to the shared [Workout] domain model. */
internal fun WorkoutLog.toWorkout(): Workout = Workout(
    id = id,
    date = date,
    type = type,
    durationMinutes = durationMinutes,
    intensity = intensity,
    notes = notes,
)
