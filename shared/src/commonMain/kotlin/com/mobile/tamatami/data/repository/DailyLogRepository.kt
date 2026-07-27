package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.mobile.tamatami.db.SleepLog
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.SleepSummary
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.sleep.SleepQualityEstimator
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Everything the user logs for a single day, backed by SQLDelight. Same public
 * API and combine/build behavior as the old Room-based repository.
 */
class DailyLogRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Everything the user has logged for [date], live. Seven sources combine
     * into one [DailySnapshot] so the Calendar's day card and Home's "today"
     * widgets can bind a single flow.
     *
     * Built as a 5-arg [combine] (the largest typed overload) zipped with the
     * remaining two flows — keeps the lambda parameters type-checked instead of
     * unpacking an untyped `Array<*>`.
     */
    fun observeToday(date: LocalDate): Flow<DailySnapshot> = combine(
        db.waterLogQueries.observeByDate(date).asFlow().mapToOneOrNull(dispatcher),
        db.moodLogQueries.observeByDate(date).asFlow().mapToOneOrNull(dispatcher),
        db.periodDayQueries.observeByDate(date).asFlow().mapToOneOrNull(dispatcher),
        db.symptomLogQueries.observeByDate(date).asFlow().mapToList(dispatcher),
        db.cravingLogQueries.observeByDate(date).asFlow().mapToOneOrNull(dispatcher),
    ) { water, mood, flow, symptoms, craving ->
        DailySnapshot(
            date = date,
            waterGlasses = water?.glasses ?: 0,
            waterGoal = water?.goal ?: 8,
            mood = mood?.mood,
            energy = mood?.energy,
            moodNotes = mood?.notes,
            periodFlow = flow?.flow,
            symptoms = symptoms.mapTo(LinkedHashSet()) { it.symptom },
            craving = craving?.craving,
            workouts = emptyList(),
            sleep = null,
        )
    }.combine(
        db.workoutLogQueries.observeByDate(date).asFlow().mapToList(dispatcher),
    ) { snapshot, workouts ->
        snapshot.copy(workouts = workouts.map { it.toWorkout() })
    }.combine(
        db.sleepLogQueries.observeByDate(date).asFlow().mapToOneOrNull(dispatcher),
    ) { snapshot, sleep ->
        snapshot.copy(sleep = sleep?.toSummary())
    }

    suspend fun incrementWater(date: LocalDate, goal: Int = 8) {
        withContext(dispatcher) {
            val current = db.waterLogQueries.observeByDate(date).executeAsOneOrNull()
            val next = (current?.glasses ?: 0) + 1
            db.waterLogQueries.upsert(
                id = current?.id,
                date = date,
                glasses = next,
                goal = goal,
            )
        }
    }

    suspend fun setWater(date: LocalDate, glasses: Int, goal: Int = 8) {
        withContext(dispatcher) {
            val current = db.waterLogQueries.observeByDate(date).executeAsOneOrNull()
            db.waterLogQueries.upsert(
                id = current?.id,
                date = date,
                glasses = glasses.coerceAtLeast(0),
                goal = goal,
            )
        }
    }

    suspend fun setMood(date: LocalDate, mood: Mood, energy: Int = 3, notes: String? = null) {
        withContext(dispatcher) {
            val current = db.moodLogQueries.observeByDate(date).executeAsOneOrNull()
            db.moodLogQueries.upsert(
                id = current?.id,
                date = date,
                mood = mood,
                energy = energy.coerceIn(1, 5),
                notes = notes ?: current?.notes,
            )
        }
    }

    /** Update only the energy slider; leaves mood + notes alone (no-op if no mood yet). */
    suspend fun setEnergy(date: LocalDate, energy: Int) {
        withContext(dispatcher) {
            val current = db.moodLogQueries.observeByDate(date).executeAsOneOrNull() ?: return@withContext
            db.moodLogQueries.upsert(
                id = current.id,
                date = current.date,
                mood = current.mood,
                energy = energy.coerceIn(1, 5),
                notes = current.notes,
            )
        }
    }

    suspend fun setFlow(date: LocalDate, flow: PeriodFlow) {
        withContext(dispatcher) {
            if (flow == PeriodFlow.NONE) {
                db.periodDayQueries.deleteByDate(date)
            } else {
                // `date` is unique (INSERT OR REPLACE) so a plain insert upserts by date.
                db.periodDayQueries.insert(date = date, flow = flow)
            }
        }
    }

    /** Toggle a symptom on/off for [date]. Cheap — same row, composite PK. */
    suspend fun toggleSymptom(date: LocalDate, symptom: Symptom) {
        withContext(dispatcher) {
            val current = db.symptomLogQueries.observeByDate(date).executeAsList()
            if (current.any { it.symptom == symptom }) {
                db.symptomLogQueries.delete(date = date, symptom = symptom)
            } else {
                db.symptomLogQueries.insert(date = date, symptom = symptom)
            }
        }
    }

    /** Pass `null` to clear the day's craving. */
    suspend fun setCraving(date: LocalDate, craving: CravingHint?) {
        withContext(dispatcher) {
            if (craving == null) {
                db.cravingLogQueries.deleteByDate(date)
                return@withContext
            }
            // `date` is unique (INSERT OR REPLACE) so a plain insert upserts by date.
            db.cravingLogQueries.insert(date = date, craving = craving)
        }
    }

    /**
     * Log a workout against [date]. Mirrors [WorkoutRepository.logWorkout] so
     * the Calendar day card can add a session for any day inline. Multiple
     * workouts per day are allowed (autogenerated id), unlike the single-row
     * mood/water logs.
     */
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

    suspend fun deleteWorkout(id: Long) {
        withContext(dispatcher) { db.workoutLogQueries.deleteById(id) }
    }

    /** Recent nights (newest first) as summaries — feeds the expected-quality prediction. */
    fun observeRecentSleep(limit: Int = 14): Flow<List<SleepSummary>> =
        db.sleepLogQueries.observeRecent(limit.toLong()).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toSummary() } }

    /**
     * Log last night's sleep against [date] (the wake-up day). Bed/wake are
     * minute-of-day; duration is computed here and already handles crossing
     * midnight. One row per date (INSERT OR REPLACE overwrites).
     */
    suspend fun logSleep(
        date: LocalDate,
        bedMinuteOfDay: Int,
        wakeMinuteOfDay: Int,
        rating: SleepRating,
    ) {
        withContext(dispatcher) {
            db.sleepLogQueries.insert(
                date = date,
                bedMinuteOfDay = bedMinuteOfDay,
                wakeMinuteOfDay = wakeMinuteOfDay,
                durationMinutes = SleepQualityEstimator.durationMinutes(bedMinuteOfDay, wakeMinuteOfDay),
                rating = rating,
            )
        }
    }
}

private fun SleepLog.toSummary(): SleepSummary = SleepSummary(
    bedMinuteOfDay = bedMinuteOfDay,
    wakeMinuteOfDay = wakeMinuteOfDay,
    durationMinutes = durationMinutes,
    rating = rating,
    quality = SleepQualityEstimator.estimate(durationMinutes, rating),
)
