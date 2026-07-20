package com.mobile.tamatami.data.backup

import com.mobile.tamatami.data.db.entity.CravingLogEntity
import com.mobile.tamatami.data.db.entity.CycleEntryEntity
import com.mobile.tamatami.data.db.entity.HormoneLogEntity
import com.mobile.tamatami.data.db.entity.MoodLogEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.SymptomLogEntity
import com.mobile.tamatami.data.db.entity.TamagotchiStateEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.data.db.entity.WaterLogEntity
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

/**
 * The on-file backup format. A single versioned envelope carrying one list per
 * Room table. DTOs use only primitives — dates as epoch-day / epoch-milli
 * [Long], enums as their `.name` [String] — so the JSON is stable and readable
 * regardless of how Room stores things internally, and never depends on the
 * entity classes being serializable.
 *
 * Bump [BACKUP_VERSION] on any breaking format change and handle older files in
 * [BackupRepository.import] if backward compatibility is ever needed.
 */
const val BACKUP_TYPE = "tamatami-backup"
const val BACKUP_VERSION = 1

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

// ----------------------------------------------------- entity <-> DTO mapping
// Kept next to the DTOs so the round-trip is defined in one place. Enum parsing
// uses valueOf, matching the app's Room TypeConverters.

fun UserProfileEntity.toDto() = UserProfileDto(
    id, tamaName, lastPeriodStart.toEpochDay(), avgCycleLengthDays, avgPeriodLengthDays,
    tryingToConceive, onContraception, irregularCycles, onboardingComplete, createdAt.toEpochMilli(),
)

fun UserProfileDto.toEntity() = UserProfileEntity(
    id = id,
    tamaName = tamaName,
    lastPeriodStart = LocalDate.ofEpochDay(lastPeriodStartEpochDay),
    avgCycleLengthDays = avgCycleLengthDays,
    avgPeriodLengthDays = avgPeriodLengthDays,
    tryingToConceive = tryingToConceive,
    onContraception = onContraception,
    irregularCycles = irregularCycles,
    onboardingComplete = onboardingComplete,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
)

fun TamagotchiStateEntity.toDto() = TamagotchiStateDto(id, lastMood.name, hatched, accessoriesJson)
fun TamagotchiStateDto.toEntity() = TamagotchiStateEntity(
    id = id,
    lastMood = TamagotchiMood.valueOf(lastMood),
    hatched = hatched,
    accessoriesJson = accessoriesJson,
)

fun CycleEntryEntity.toDto() = CycleEntryDto(id, startDate.toEpochDay(), endDate?.toEpochDay(), lengthDays)
fun CycleEntryDto.toEntity() = CycleEntryEntity(
    id = id,
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    endDate = endDateEpochDay?.let(LocalDate::ofEpochDay),
    lengthDays = lengthDays,
)

fun PeriodDayEntity.toDto() = PeriodDayDto(id, date.toEpochDay(), flow.name)
fun PeriodDayDto.toEntity() = PeriodDayEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    flow = PeriodFlow.valueOf(flow),
)

fun MoodLogEntity.toDto() = MoodLogDto(id, date.toEpochDay(), mood.name, energy, notes)
fun MoodLogDto.toEntity() = MoodLogEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    mood = Mood.valueOf(mood),
    energy = energy,
    notes = notes,
)

fun WaterLogEntity.toDto() = WaterLogDto(id, date.toEpochDay(), glasses, goal)
fun WaterLogDto.toEntity() = WaterLogEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    glasses = glasses,
    goal = goal,
)

fun HormoneLogEntity.toDto() = HormoneLogDto(id, date.toEpochDay(), hormone, value, unit, notes)
fun HormoneLogDto.toEntity() = HormoneLogEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    hormone = hormone,
    value = value,
    unit = unit,
    notes = notes,
)

fun WorkoutLogEntity.toDto() = WorkoutLogDto(
    id, date.toEpochDay(), type.name, durationMinutes, intensity.name, notes,
)
fun WorkoutLogDto.toEntity() = WorkoutLogEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    type = WorkoutType.valueOf(type),
    durationMinutes = durationMinutes,
    intensity = WorkoutIntensity.valueOf(intensity),
    notes = notes,
)

fun SymptomLogEntity.toDto() = SymptomLogDto(date.toEpochDay(), symptom.name)
fun SymptomLogDto.toEntity() = SymptomLogEntity(
    date = LocalDate.ofEpochDay(dateEpochDay),
    symptom = Symptom.valueOf(symptom),
)

fun CravingLogEntity.toDto() = CravingLogDto(id, date.toEpochDay(), craving.name)
fun CravingLogDto.toEntity() = CravingLogEntity(
    id = id,
    date = LocalDate.ofEpochDay(dateEpochDay),
    craving = CravingHint.valueOf(craving),
)
