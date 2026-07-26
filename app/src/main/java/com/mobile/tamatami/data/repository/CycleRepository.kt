package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.CycleEntryDao
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.db.dao.UserProfileDao
import com.mobile.tamatami.data.db.entity.CycleEntryEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

class CycleRepository(
    private val userDao: UserProfileDao,
    private val cycleDao: CycleEntryDao,
    private val periodDayDao: PeriodDayDao,
) {
    /**
     * Live snapshot for [today], recomputed whenever the profile or any
     * logged period day changes. Adaptive: uses the user's logged history
     * (via [CyclePhaseCalculator.calculateAdaptive]) so the predicted-next
     * date reflects reality once a few cycles are recorded.
     */
    fun observeTodayCycle(today: LocalDate): Flow<CycleSnapshot> =
        combine(userDao.observe(), periodDayDao.observeRecent()) { profile, periodDays ->
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
                CyclePhaseCalculator.calculateAdaptive(
                    profile = CycleProfile(
                        lastPeriodStart = profile.lastPeriodStart.toKotlinLocalDate(),
                        avgCycleLengthDays = profile.avgCycleLengthDays,
                        avgPeriodLengthDays = profile.avgPeriodLengthDays,
                    ),
                    loggedPeriodDays = periodDays.map {
                        PeriodDay(it.date.toKotlinLocalDate(), it.flow)
                    },
                    today = today.toKotlinLocalDate(),
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
