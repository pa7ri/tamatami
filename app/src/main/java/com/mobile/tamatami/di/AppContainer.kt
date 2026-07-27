package com.mobile.tamatami.di

import android.content.Context
import com.mobile.tamatami.data.backup.BackupRepository
import com.mobile.tamatami.data.db.DatabaseFactory
import com.mobile.tamatami.data.db.DriverFactory
import com.mobile.tamatami.data.health.StepDataSource
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.HormoneRepository
import com.mobile.tamatami.data.repository.MedicationRepository
import com.mobile.tamatami.data.repository.TamagotchiRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.data.repository.WorkoutRepository
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.notifications.Notifier
import com.mobile.tamatami.notifications.ReminderScheduler
import com.mobile.tamatami.util.Clock
import com.mobile.tamatami.util.SystemClock

/**
 * Hand-rolled DI container. Lives on [com.mobile.tamatami.TamatamiApp]; lazy
 * by design so cold-start work is minimal. The data layer is now the shared
 * SQLDelight-backed [TamatamiDb] + repositories from the :shared module.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val clock: Clock = SystemClock

    private val db: TamatamiDb by lazy {
        DatabaseFactory.create(DriverFactory(appContext))
    }

    val userRepository: UserRepository by lazy { UserRepository(db) }

    val cycleRepository: CycleRepository by lazy { CycleRepository(db) }

    val dailyLogRepository: DailyLogRepository by lazy { DailyLogRepository(db) }

    val tamagotchiRepository: TamagotchiRepository by lazy {
        TamagotchiRepository(cycleRepository, dailyLogRepository)
    }

    val hormoneRepository: HormoneRepository by lazy { HormoneRepository(db) }

    val workoutRepository: WorkoutRepository by lazy { WorkoutRepository(db) }

    val medicationRepository: MedicationRepository by lazy { MedicationRepository(db) }

    val backupRepository: BackupRepository by lazy { BackupRepository(db) }

    val stepDataSource: StepDataSource by lazy { StepDataSource(appContext) }

    val notifier: Notifier by lazy { Notifier(appContext) }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext) }
}
