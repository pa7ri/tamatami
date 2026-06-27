package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.CycleEntryDao
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.db.dao.UserProfileDao
import com.mobile.tamatami.data.db.entity.CycleEntryEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class CycleRepository(
    private val userDao: UserProfileDao,
    private val cycleDao: CycleEntryDao,
    private val periodDayDao: PeriodDayDao,
) {
    /** Live snapshot for [today], recomputed whenever the user profile changes. */
    fun observeTodayCycle(today: LocalDate): Flow<CycleSnapshot> =
        userDao.observe().map { profile ->
            if (profile == null) {
                CycleSnapshot(
                    phase = CyclePhase.UNKNOWN,
                    cycleDay = 0,
                    dayInPhase = 0,
                    predictedNextPeriod = null,
                    daysUntilNextPeriod = null,
                    cycleLength = 28,
                )
            } else {
                CyclePhaseCalculator.calculate(
                    lastPeriodStart = profile.lastPeriodStart,
                    avgCycleLength = profile.avgCycleLengthDays,
                    avgPeriodLength = profile.avgPeriodLengthDays,
                    today = today,
                )
            }
        }

    suspend fun logPeriodDay(date: LocalDate, flow: PeriodFlow) {
        if (flow == PeriodFlow.NONE) {
            periodDayDao.deleteByDate(date)
        } else {
            periodDayDao.upsert(PeriodDayEntity(date = date, flow = flow))
        }
    }

    suspend fun seedCycleEntry(start: LocalDate, length: Int) {
        if (cycleDao.byStart(start) == null) {
            cycleDao.upsert(CycleEntryEntity(startDate = start, endDate = null, lengthDays = length))
        }
    }
}
