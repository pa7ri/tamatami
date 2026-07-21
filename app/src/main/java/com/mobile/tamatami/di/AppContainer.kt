package com.mobile.tamatami.di

import android.content.Context
import androidx.room.Room
import com.mobile.tamatami.data.backup.BackupRepository
import com.mobile.tamatami.data.db.TamatamiDatabase
import com.mobile.tamatami.data.health.StepDataSource
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.HormoneRepository
import com.mobile.tamatami.data.repository.TamagotchiRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.data.repository.WorkoutRepository
import com.mobile.tamatami.util.Clock
import com.mobile.tamatami.util.SystemClock

/**
 * Hand-rolled DI container. Lives on [com.mobile.tamatami.TamatamiApp]; lazy
 * by design so cold-start work is minimal. Migration to Hilt later is mostly
 * mechanical — every property here becomes an `@Provides` in a module.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val clock: Clock = SystemClock

    private val db: TamatamiDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            TamatamiDatabase::class.java,
            TamatamiDatabase.NAME,
        )
            // For the vertical slice we accept destructive migrations between
            // local-only dev builds. Add real migrations before public release.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val userRepository: UserRepository by lazy { UserRepository(db.userProfileDao()) }

    /** Exposed for screens (e.g. Calendar) that need raw period-day observation. */
    val periodDayDao: PeriodDayDao by lazy { db.periodDayDao() }

    val cycleRepository: CycleRepository by lazy {
        CycleRepository(
            userDao = db.userProfileDao(),
            cycleDao = db.cycleEntryDao(),
            periodDayDao = db.periodDayDao(),
        )
    }

    val dailyLogRepository: DailyLogRepository by lazy {
        DailyLogRepository(
            waterDao = db.waterLogDao(),
            moodDao = db.moodLogDao(),
            periodDayDao = db.periodDayDao(),
            symptomDao = db.symptomLogDao(),
            cravingDao = db.cravingLogDao(),
            workoutDao = db.workoutLogDao(),
            sleepDao = db.sleepLogDao(),
        )
    }

    val tamagotchiRepository: TamagotchiRepository by lazy {
        TamagotchiRepository(cycleRepository, dailyLogRepository)
    }

    val hormoneRepository: HormoneRepository by lazy {
        HormoneRepository(db.hormoneLogDao())
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(db.workoutLogDao())
    }

    val backupRepository: BackupRepository by lazy { BackupRepository(db) }

    val stepDataSource: StepDataSource by lazy { StepDataSource(appContext) }
}
