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
import com.mobile.tamatami.db.UserProfile
import com.mobile.tamatami.domain.calendar.CalendarDay
import com.mobile.tamatami.domain.calendar.MonthBuilder
import com.mobile.tamatami.domain.model.CycleProfile
import com.mobile.tamatami.domain.model.CycleSnapshot
import com.mobile.tamatami.domain.model.PeriodDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    /** Build a [LocalDate] from y/m/d — lets Swift pass DateComponents ints
     *  rather than constructing the bridged kotlinx type directly. */
    fun localDate(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

    /**
     * Whether onboarding is complete, as a non-null [Boolean] Flow. Avoids
     * bridging a `Flow<UserProfile?>` (nullable generic) to Swift, which is
     * fragile in Kotlin/Native — the root gate observes this instead.
     */
    fun observeOnboardingComplete(): Flow<Boolean> =
        user.observeProfile().map { it?.onboardingComplete ?: false }

    /**
     * The 6×7 calendar grid for [year]/[monthNumber] (1-12) as a Flow, rebuilt
     * whenever the profile or logged period days change. Runs the shared
     * [MonthBuilder]; the SwiftUI grid just renders the 42 [CalendarDay]s.
     */
    fun observeMonth(year: Int, monthNumber: Int): Flow<List<CalendarDay>> =
        combine(
            user.observeProfile(),
            cycle.observeRecentPeriodDays(),
        ) { profile, periodDays ->
            MonthBuilder.build(
                year = year,
                monthNumber = monthNumber,
                profile = profile?.let {
                    CycleProfile(
                        lastPeriodStart = it.lastPeriodStart,
                        avgCycleLengthDays = it.avgCycleLengthDays,
                        avgPeriodLengthDays = it.avgPeriodLengthDays,
                    )
                },
                loggedPeriodDays = periodDays,
                today = today(),
            )
        }

    /** One-shot cycle snapshot for [date] — for the notification scheduler,
     *  which needs a single value rather than a Flow subscription. */
    suspend fun currentCycle(date: LocalDate): CycleSnapshot =
        cycle.observeTodayCycle(date).first()

    /**
     * Create + persist the profile from onboarding answers, and seed the first
     * cycle entry. Keeps the 17-field [UserProfile] construction in one place so
     * the SwiftUI layer only passes the handful of user-entered values.
     */
    suspend fun completeOnboarding(
        tamaName: String,
        lastPeriodStart: LocalDate,
        avgCycleLengthDays: Int,
        avgPeriodLengthDays: Int,
        tryingToConceive: Boolean,
        onContraception: Boolean,
        irregularCycles: Boolean,
    ) {
        user.saveProfile(
            UserProfile(
                id = 0,
                tamaName = tamaName.ifBlank { "Tama" },
                lastPeriodStart = lastPeriodStart,
                avgCycleLengthDays = avgCycleLengthDays,
                avgPeriodLengthDays = avgPeriodLengthDays,
                tryingToConceive = tryingToConceive,
                onContraception = onContraception,
                irregularCycles = irregularCycles,
                onboardingComplete = true,
                createdAt = Clock.System.now(),
                dailyStepsGoal = 8_000,
                sleepGoalMinutes = 8 * 60,
                waterGoalGlasses = 8,
                remindPeriodEnabled = true,
                remindWaterEnabled = false,
                waterReminderIntervalHours = 3,
                remindPillsEnabled = true,
            )
        )
        cycle.seedCycleEntry(lastPeriodStart, avgCycleLengthDays)
    }

    /**
     * Read-modify-write the profile for the Settings screen. Only the fields the
     * UI edits are parameters; everything else (createdAt, onboardingComplete) is
     * preserved. No-op if there's no profile yet.
     */
    suspend fun updateSettings(
        tamaName: String,
        avgCycleLengthDays: Int,
        avgPeriodLengthDays: Int,
        dailyStepsGoal: Int,
        sleepGoalMinutes: Int,
        waterGoalGlasses: Int,
        remindPeriodEnabled: Boolean,
        remindWaterEnabled: Boolean,
        remindPillsEnabled: Boolean,
    ) {
        val current = user.getProfile() ?: return
        user.saveProfile(
            current.copy(
                tamaName = tamaName.ifBlank { "Tama" },
                avgCycleLengthDays = avgCycleLengthDays,
                avgPeriodLengthDays = avgPeriodLengthDays,
                dailyStepsGoal = dailyStepsGoal,
                sleepGoalMinutes = sleepGoalMinutes,
                waterGoalGlasses = waterGoalGlasses,
                remindPeriodEnabled = remindPeriodEnabled,
                remindWaterEnabled = remindWaterEnabled,
                remindPillsEnabled = remindPillsEnabled,
            )
        )
    }
}
