package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.db.UserProfile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * User profile (singleton row, id = 0). Now backed by SQLDelight and shared
 * across platforms; the generated [UserProfile] row type replaces the old Room
 * entity and carries the same fields (with kotlinx-datetime types).
 */
class UserRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun observeProfile(): Flow<UserProfile?> =
        db.userProfileQueries.get().asFlow().mapToOneOrNull(dispatcher)

    suspend fun getProfile(): UserProfile? =
        withContext(dispatcher) { db.userProfileQueries.get().executeAsOneOrNull() }

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileQueries.upsert(
            tamaName = profile.tamaName,
            lastPeriodStart = profile.lastPeriodStart,
            avgCycleLengthDays = profile.avgCycleLengthDays,
            avgPeriodLengthDays = profile.avgPeriodLengthDays,
            tryingToConceive = profile.tryingToConceive,
            onContraception = profile.onContraception,
            irregularCycles = profile.irregularCycles,
            onboardingComplete = profile.onboardingComplete,
            createdAt = profile.createdAt,
            dailyStepsGoal = profile.dailyStepsGoal,
            sleepGoalMinutes = profile.sleepGoalMinutes,
            waterGoalGlasses = profile.waterGoalGlasses,
            remindPeriodEnabled = profile.remindPeriodEnabled,
            remindWaterEnabled = profile.remindWaterEnabled,
            waterReminderIntervalHours = profile.waterReminderIntervalHours,
            remindPillsEnabled = profile.remindPillsEnabled,
        )
    }
}
