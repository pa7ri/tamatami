package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.CravingLogDao
import com.mobile.tamatami.data.db.dao.MoodLogDao
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.db.dao.SleepLogDao
import com.mobile.tamatami.data.db.dao.SymptomLogDao
import com.mobile.tamatami.data.db.dao.WaterLogDao
import com.mobile.tamatami.data.db.dao.WorkoutLogDao
import com.mobile.tamatami.data.db.entity.CravingLogEntity
import com.mobile.tamatami.data.db.entity.MoodLogEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.SleepLogEntity
import com.mobile.tamatami.data.db.entity.SymptomLogEntity
import com.mobile.tamatami.data.db.entity.WaterLogEntity
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.SleepSummary
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.model.Workout
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.sleep.SleepQualityEstimator
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

class DailyLogRepository(
    private val waterDao: WaterLogDao,
    private val moodDao: MoodLogDao,
    private val periodDayDao: PeriodDayDao,
    private val symptomDao: SymptomLogDao,
    private val cravingDao: CravingLogDao,
    private val workoutDao: WorkoutLogDao,
    private val sleepDao: SleepLogDao,
) {
    /**
     * Everything the user has logged for [date], live. Six sources combine
     * into one [DailySnapshot] so the Calendar's day card and Home's "today"
     * widgets can bind a single flow.
     *
     * Built as a 5-arg [combine] (the largest typed overload) zipped with the
     * sixth flow — keeps the lambda parameters type-checked instead of
     * unpacking an untyped `Array<*>`.
     */
    fun observeToday(date: LocalDate): Flow<DailySnapshot> = combine(
        waterDao.observeByDate(date),
        moodDao.observeByDate(date),
        periodDayDao.observeByDate(date),
        symptomDao.observeByDate(date),
        cravingDao.observeByDate(date),
    ) { water, mood, flow, symptoms, craving ->
        DailySnapshot(
            date = date.toKotlinLocalDate(),
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
    }.combine(workoutDao.observeByDate(date)) { snapshot, workouts ->
        snapshot.copy(workouts = workouts.map { it.toWorkout() })
    }.combine(sleepDao.observeByDate(date)) { snapshot, sleep ->
        snapshot.copy(sleep = sleep?.toSummary())
    }

    suspend fun incrementWater(date: LocalDate, goal: Int = 8) {
        val current = waterDao.observeByDate(date).first()
        val next = (current?.glasses ?: 0) + 1
        waterDao.upsert(
            WaterLogEntity(
                id = current?.id ?: 0,
                date = date,
                glasses = next,
                goal = goal,
            )
        )
    }

    suspend fun setWater(date: LocalDate, glasses: Int, goal: Int = 8) {
        val current = waterDao.observeByDate(date).first()
        waterDao.upsert(
            WaterLogEntity(
                id = current?.id ?: 0,
                date = date,
                glasses = glasses.coerceAtLeast(0),
                goal = goal,
            )
        )
    }

    suspend fun setMood(date: LocalDate, mood: Mood, energy: Int = 3, notes: String? = null) {
        val current = moodDao.observeByDate(date).first()
        moodDao.upsert(
            MoodLogEntity(
                id = current?.id ?: 0,
                date = date,
                mood = mood,
                energy = energy.coerceIn(1, 5),
                notes = notes ?: current?.notes,
            )
        )
    }

    /** Update only the energy slider; leaves mood + notes alone (no-op if no mood yet). */
    suspend fun setEnergy(date: LocalDate, energy: Int) {
        val current = moodDao.observeByDate(date).first() ?: return
        moodDao.upsert(current.copy(energy = energy.coerceIn(1, 5)))
    }

    suspend fun setFlow(date: LocalDate, flow: PeriodFlow) {
        if (flow == PeriodFlow.NONE) {
            periodDayDao.deleteByDate(date)
        } else {
            val current = periodDayDao.observeByDate(date).first()
            periodDayDao.upsert(
                PeriodDayEntity(
                    id = current?.id ?: 0,
                    date = date,
                    flow = flow,
                )
            )
        }
    }

    /** Toggle a symptom on/off for [date]. Cheap — same row, composite PK. */
    suspend fun toggleSymptom(date: LocalDate, symptom: Symptom) {
        val current = symptomDao.observeByDate(date).first()
        if (current.any { it.symptom == symptom }) {
            symptomDao.delete(date, symptom)
        } else {
            symptomDao.upsert(SymptomLogEntity(date, symptom))
        }
    }

    /** Pass `null` to clear the day's craving. */
    suspend fun setCraving(date: LocalDate, craving: CravingHint?) {
        if (craving == null) {
            cravingDao.deleteByDate(date)
            return
        }
        val current = cravingDao.observeByDate(date).first()
        cravingDao.upsert(
            CravingLogEntity(
                id = current?.id ?: 0,
                date = date,
                craving = craving,
            )
        )
    }

    /**
     * Log a workout against [date]. Mirrors [WorkoutRepository.logWorkout] so
     * the Calendar day card can add a session for any day inline without
     * pulling in the Training screen's repository. Multiple workouts per day
     * are allowed (autogenerated id), unlike the single-row mood/water logs.
     */
    suspend fun logWorkout(
        date: LocalDate,
        type: WorkoutType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        notes: String? = null,
        id: Long = 0,
    ) {
        workoutDao.upsert(
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

    suspend fun deleteWorkout(id: Long) = workoutDao.deleteById(id)

    /** Recent nights (newest first) as summaries — feeds the expected-quality prediction. */
    fun observeRecentSleep(limit: Int = 14): Flow<List<SleepSummary>> =
        sleepDao.observeRecent(limit).map { rows -> rows.map { it.toSummary() } }

    /**
     * Log last night's sleep against [date] (the wake-up day). Bed/wake are
     * minute-of-day; duration is computed here and already handles crossing
     * midnight. One row per date (upsert overwrites).
     */
    suspend fun logSleep(
        date: LocalDate,
        bedMinuteOfDay: Int,
        wakeMinuteOfDay: Int,
        rating: SleepRating,
    ) {
        val current = sleepDao.observeByDate(date).first()
        sleepDao.upsert(
            SleepLogEntity(
                id = current?.id ?: 0,
                date = date,
                bedMinuteOfDay = bedMinuteOfDay,
                wakeMinuteOfDay = wakeMinuteOfDay,
                durationMinutes = SleepQualityEstimator.durationMinutes(bedMinuteOfDay, wakeMinuteOfDay),
                rating = rating,
            )
        )
    }
}

private fun SleepLogEntity.toSummary(): SleepSummary = SleepSummary(
    bedMinuteOfDay = bedMinuteOfDay,
    wakeMinuteOfDay = wakeMinuteOfDay,
    durationMinutes = durationMinutes,
    rating = rating,
    quality = SleepQualityEstimator.estimate(durationMinutes, rating),
)

/** Map the Room workout row to the shared [Workout] domain model. */
internal fun WorkoutLogEntity.toWorkout(): Workout = Workout(
    id = id,
    date = date.toKotlinLocalDate(),
    type = type,
    durationMinutes = durationMinutes,
    intensity = intensity,
    notes = notes,
)
