package com.mobile.tamatami.data.backup

import com.mobile.tamatami.db.CravingLog
import com.mobile.tamatami.db.CycleEntry
import com.mobile.tamatami.db.HormoneLog
import com.mobile.tamatami.db.Medication
import com.mobile.tamatami.db.MedicationIntake
import com.mobile.tamatami.db.MoodLog
import com.mobile.tamatami.db.PeriodDay
import com.mobile.tamatami.db.SleepLog
import com.mobile.tamatami.db.SymptomLog
import com.mobile.tamatami.db.TamagotchiState
import com.mobile.tamatami.db.UserProfile
import com.mobile.tamatami.db.WaterLog
import com.mobile.tamatami.db.WorkoutLog
import kotlinx.serialization.Serializable

/**
 * The on-file backup format. A single versioned envelope carrying one list per
 * table. DTOs use only primitives — dates as epoch-day / epoch-milli
 * [Long], enums as their `.name` [String] — so the JSON is stable and readable
 * regardless of how the DB stores things internally, and never depends on the
 * generated row classes being serializable.
 *
 * Bump [BACKUP_VERSION] on any breaking format change and handle older files in
 * [BackupRepository.import] if backward compatibility is ever needed.
 */
const val BACKUP_TYPE = "tamatami-backup"
const val BACKUP_VERSION = 4

@Serializable
data class TamatamiBackup(
    val type: String = BACKUP_TYPE,
    val version: Int = BACKUP_VERSION,
    val exportedAtEpochMs: Long,
    val userProfile: UserProfileDto? = null,
    val tamagotchi: TamagotchiStateDto? = null,
    val cycleEntries: List<CycleEntryDto> = emptyList(),
    val periodDays: List<PeriodDayDto> = emptyList(),
    val moods: List<MoodLogDto> = emptyList(),
    val water: List<WaterLogDto> = emptyList(),
    val hormones: List<HormoneLogDto> = emptyList(),
    val workouts: List<WorkoutLogDto> = emptyList(),
    val symptoms: List<SymptomLogDto> = emptyList(),
    val cravings: List<CravingLogDto> = emptyList(),
    val sleeps: List<SleepLogDto> = emptyList(),
    val medications: List<MedicationDto> = emptyList(),
    val medicationIntakes: List<MedicationIntakeDto> = emptyList(),
)

// ---------------------------------------------------------------- per-table DTOs

@Serializable
data class UserProfileDto(
    val id: Int,
    val tamaName: String,
    val lastPeriodStartEpochDay: Long,
    val avgCycleLengthDays: Int,
    val avgPeriodLengthDays: Int,
    val tryingToConceive: Boolean,
    val onContraception: Boolean,
    val irregularCycles: Boolean,
    val onboardingComplete: Boolean,
    val createdAtEpochMs: Long,
    val dailyStepsGoal: Int = 8_000,
    val sleepGoalMinutes: Int = 8 * 60,
    val waterGoalGlasses: Int = 8,
)

@Serializable
data class TamagotchiStateDto(
    val id: Int,
    val lastMood: String,
    val hatched: Boolean,
    val accessoriesJson: String,
)

@Serializable
data class CycleEntryDto(
    val id: Long,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long?,
    val lengthDays: Int?,
)

@Serializable
data class PeriodDayDto(
    val id: Long,
    val dateEpochDay: Long,
    val flow: String,
)

@Serializable
data class MoodLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val mood: String,
    val energy: Int,
    val notes: String?,
)

@Serializable
data class WaterLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val glasses: Int,
    val goal: Int,
)

@Serializable
data class HormoneLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val hormone: String,
    val value: Float,
    val unit: String,
    val notes: String?,
)

@Serializable
data class WorkoutLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val type: String,
    val durationMinutes: Int,
    val intensity: String,
    val notes: String?,
)

@Serializable
data class SymptomLogDto(
    val dateEpochDay: Long,
    val symptom: String,
)

@Serializable
data class CravingLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val craving: String,
)

@Serializable
data class SleepLogDto(
    val id: Long,
    val dateEpochDay: Long,
    val bedMinuteOfDay: Int,
    val wakeMinuteOfDay: Int,
    val durationMinutes: Int,
    val rating: String,
)

@Serializable
data class MedicationDto(
    val id: Long,
    val name: String,
    val dosesPerDay: Int,
    val slotsMask: Int,
    val active: Boolean,
    val createdAtEpochMs: Long,
)

@Serializable
data class MedicationIntakeDto(
    val id: Long,
    val medicationId: Long,
    val dateEpochDay: Long,
    val slot: String,
)

// ----------------------------------------------------- row -> DTO mapping
// Kept next to the DTOs so the export shape is defined in one place. Enum
// values are stored as their `.name` string. Dates/instants use kotlinx.datetime
// epoch conversions. The reverse (DTO -> row) is done inline in BackupRepository,
// which repopulates via the generated insert/upsert query functions.

fun UserProfile.toDto() = UserProfileDto(
    id, tamaName, lastPeriodStart.toEpochDays().toLong(), avgCycleLengthDays, avgPeriodLengthDays,
    tryingToConceive, onContraception, irregularCycles, onboardingComplete, createdAt.toEpochMilliseconds(),
    dailyStepsGoal, sleepGoalMinutes, waterGoalGlasses,
)

fun TamagotchiState.toDto() = TamagotchiStateDto(id, lastMood.name, hatched, accessoriesJson)

fun CycleEntry.toDto() = CycleEntryDto(
    id, startDate.toEpochDays().toLong(), endDate?.toEpochDays()?.toLong(), lengthDays,
)

fun PeriodDay.toDto() = PeriodDayDto(id, date.toEpochDays().toLong(), flow.name)

fun MoodLog.toDto() = MoodLogDto(id, date.toEpochDays().toLong(), mood.name, energy, notes)

fun WaterLog.toDto() = WaterLogDto(id, date.toEpochDays().toLong(), glasses, goal)

fun HormoneLog.toDto() = HormoneLogDto(id, date.toEpochDays().toLong(), hormone, value_, unit, notes)

fun WorkoutLog.toDto() = WorkoutLogDto(
    id, date.toEpochDays().toLong(), type.name, durationMinutes, intensity.name, notes,
)

fun SymptomLog.toDto() = SymptomLogDto(date.toEpochDays().toLong(), symptom.name)

fun CravingLog.toDto() = CravingLogDto(id, date.toEpochDays().toLong(), craving.name)

fun SleepLog.toDto() = SleepLogDto(
    id, date.toEpochDays().toLong(), bedMinuteOfDay, wakeMinuteOfDay, durationMinutes, rating.name,
)

fun Medication.toDto() = MedicationDto(
    id, name, dosesPerDay, slotsMask, active, createdAt.toEpochMilliseconds(),
)

fun MedicationIntake.toDto() = MedicationIntakeDto(id, medicationId, date.toEpochDays().toLong(), slot.name)
