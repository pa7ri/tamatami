package com.mobile.tamatami.ios

import com.mobile.tamatami.data.db.DatabaseFactory
import com.mobile.tamatami.data.db.DriverFactory
import com.mobile.tamatami.data.repository.CycleRepository
import com.mobile.tamatami.data.repository.DailyLogRepository
import com.mobile.tamatami.data.repository.HormoneRepository
import com.mobile.tamatami.data.repository.MedicationRepository
import com.mobile.tamatami.data.repository.TamagotchiRepository
import com.mobile.tamatami.data.repository.UserRepository
import com.mobile.tamatami.data.repository.WorkoutRepository
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.model.CycleSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * One-call bootstrap for the SwiftUI app. Builds the shared SQLDelight database
 * with the iOS native driver and exposes every repository. Hold a single
 * instance for the app's lifetime (e.g. in the SwiftUI `App` struct).
 *
 * Suspend repository functions bridge to Swift as `async` automatically;
 * `Flow`-returning functions should be wrapped with [FlowObserver] on the Swift
 * side. Reminder decisions come from the shared `ReminderEvaluator`; scheduling
 * the actual local notifications is done natively in Swift
 * (UNUserNotificationCenter) — see the iOS app's NotificationScheduler.
 */
class TamatamiSdk {

    private val db: TamatamiDb = DatabaseFactory.create(DriverFactory())

    val user: UserRepository = UserRepository(db)
    val cycle: CycleRepository = CycleRepository(db)
    val daily: DailyLogRepository = DailyLogRepository(db)
    val hormones: HormoneRepository = HormoneRepository(db)
    val workouts: WorkoutRepository = WorkoutRepository(db)
    val medication: MedicationRepository = MedicationRepository(db)
    val tamagotchi: TamagotchiRepository = TamagotchiRepository(cycle, daily)

    /** Convenience so Swift doesn't have to construct kotlinx-datetime values. */
    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    fun now(): Instant = Clock.System.now()

    /** One-shot cycle snapshot for [date] — for the notification scheduler,
     *  which needs a single value rather than a Flow subscription. */
    suspend fun currentCycle(date: LocalDate): CycleSnapshot =
        cycle.observeTodayCycle(date).first()
}
