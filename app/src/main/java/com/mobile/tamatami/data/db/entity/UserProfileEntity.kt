package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Singleton row holding the user's profile + onboarding answers. Persisting
 * `id = 0` everywhere makes upserting the row trivial.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val tamaName: String,
    val lastPeriodStart: LocalDate,
    val avgCycleLengthDays: Int,
    val avgPeriodLengthDays: Int,
    val tryingToConceive: Boolean,
    val onContraception: Boolean,
    val irregularCycles: Boolean,
    val onboardingComplete: Boolean,
    val createdAt: Instant,
)
