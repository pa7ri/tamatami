package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.MoodLogDao
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.db.dao.WaterLogDao
import com.mobile.tamatami.data.db.entity.MoodLogEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.WaterLogEntity
import com.mobile.tamatami.domain.model.DailySnapshot
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class DailyLogRepository(
    private val waterDao: WaterLogDao,
    private val moodDao: MoodLogDao,
    private val periodDayDao: PeriodDayDao,
) {
    fun observeToday(date: LocalDate): Flow<DailySnapshot> = combine(
        waterDao.observeByDate(date),
        moodDao.observeByDate(date),
        periodDayDao.observeByDate(date),
    ) { water, mood, flow ->
        DailySnapshot(
            date = date,
            waterGlasses = water?.glasses ?: 0,
            waterGoal = water?.goal ?: 8,
            mood = mood?.mood,
            energy = mood?.energy,
            periodFlow = flow?.flow,
        )
    }

    suspend fun incrementWater(date: LocalDate) {
        val current = waterDao.observeByDate(date).first()
        val next = (current?.glasses ?: 0) + 1
        waterDao.upsert(
            WaterLogEntity(
                id = current?.id ?: 0,
                date = date,
                glasses = next,
                goal = current?.goal ?: 8,
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
                notes = notes,
            )
        )
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
}
