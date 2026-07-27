package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.cycle.CyclePhaseCalculator
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.PeriodDay
import com.mobile.tamatami.domain.model.PeriodFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Cycle snapshot + period-day logging, backed by SQLDelight. Combines the
 * (singleton) user profile with the recent logged period days to produce an
 * adaptive [CycleSnapshot].
 */
class CycleRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Live snapshot for [today], recomputed whenever the profile or any
     * logged period day changes. Adaptive: uses the user's logged history
     * (via [CyclePhaseCalculator.calculateAdaptive]) so the predicted-next
     * date reflects reality once a few cycles are recorded.
     */
    fun observeTodayCycle(today: LocalDate): Flow<CycleSnapshot> =
        combine(
            db.userProfileQueries.get().asFlow().mapToOneOrNull(dispatcher),
            db.periodDayQueries.observeRecent().asFlow().mapToList(dispatcher),
        ) { profile, periodDays ->
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
                        lastPeriodStart = profile.lastPeriodStart,
                        avgCycleLengthDays = profile.avgCycleLengthDays,
                        avgPeriodLengthDays = profile.avgPeriodLengthDays,
                    ),
                    loggedPeriodDays = periodDays.map { PeriodDay(it.date, it.flow) },
                    today = today,
                )
            }
        }

    /** Recent logged period days (newest first) as domain models — feeds the calendar grid. */
    fun observeRecentPeriodDays(): Flow<List<PeriodDay>> =
        db.periodDayQueries.observeRecent().asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { PeriodDay(it.date, it.flow) } }

    suspend fun logPeriodDay(date: LocalDate, flow: PeriodFlow) {
        withContext(dispatcher) {
            if (flow == PeriodFlow.NONE) {
                db.periodDayQueries.deleteByDate(date)
            } else {
                // `date` is unique (INSERT OR REPLACE) so a plain insert upserts by date.
                db.periodDayQueries.insert(date = date, flow = flow)
            }
        }
    }

    suspend fun seedCycleEntry(start: LocalDate, length: Int) {
        withContext(dispatcher) {
            if (db.cycleEntryQueries.byStart(start).executeAsOneOrNull() == null) {
                db.cycleEntryQueries.upsert(
                    id = null,
                    startDate = start,
                    endDate = null,
                    lengthDays = length,
                )
            }
        }
    }
}
