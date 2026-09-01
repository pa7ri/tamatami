package com.mobile.tamatami.data.backup

import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/** Thrown when an imported file isn't a recognizable tamatami backup. */
class InvalidBackupException(message: String) : Exception(message)

/**
 * Full-database JSON backup and restore.
 *
 * [exportToJson] reads every table and serializes a single [TamatamiBackup]
 * envelope. [importFromJson] parses that envelope and, inside one SQLDelight
 * transaction, **replaces all data** — every table is cleared and then
 * repopulated from the file, so the DB ends up exactly matching the backup.
 *
 * Because it wipes existing rows, callers should confirm with the user first.
 */
class BackupRepository(
    private val db: TamatamiDb,
    private val json: Json = DEFAULT_JSON,
) {
    suspend fun exportToJson(exportedAtEpochMs: Long): String {
        val backup = TamatamiBackup(
            exportedAtEpochMs = exportedAtEpochMs,
            userProfile = db.userProfileQueries.get().executeAsOneOrNull()?.toDto(),
            tamagotchi = db.tamagotchiStateQueries.get().executeAsOneOrNull()?.toDto(),
            cycleEntries = db.cycleEntryQueries.getAll().executeAsList().map { it.toDto() },
            periodDays = db.periodDayQueries.getAll().executeAsList().map { it.toDto() },
            moods = db.moodLogQueries.getAll().executeAsList().map { it.toDto() },
            water = db.waterLogQueries.getAll().executeAsList().map { it.toDto() },
            hormones = db.hormoneLogQueries.getAll().executeAsList().map { it.toDto() },
            workouts = db.workoutLogQueries.getAll().executeAsList().map { it.toDto() },
            symptoms = db.symptomLogQueries.getAll().executeAsList().map { it.toDto() },
            cravings = db.cravingLogQueries.getAll().executeAsList().map { it.toDto() },
            sleeps = db.sleepLogQueries.getAll().executeAsList().map { it.toDto() },
            medications = db.medicationQueries.getAll().executeAsList().map { it.toDto() },
            medicationIntakes = db.medicationIntakeQueries.getAll().executeAsList().map { it.toDto() },
        )
        return json.encodeToString(TamatamiBackup.serializer(), backup)
    }

    /** Parse + validate only, so the UI can confirm before touching the DB. */
    fun parse(text: String): TamatamiBackup = parse(text, json)

    suspend fun importFromJson(text: String) = restore(parse(text))

    /** Clear-then-insert every table atomically. */
    suspend fun restore(backup: TamatamiBackup) {
        db.transaction {
            // Clear everything first so the result exactly matches the file.
            db.userProfileQueries.clear()
            db.tamagotchiStateQueries.clear()
            db.cycleEntryQueries.clear()
            db.periodDayQueries.clear()
            db.moodLogQueries.clear()
            db.waterLogQueries.clear()
            db.hormoneLogQueries.clear()
            db.workoutLogQueries.clear()
            db.symptomLogQueries.clear()
            db.cravingLogQueries.clear()
            db.sleepLogQueries.clear()
            // Intakes reference a medication id, so clear them before the meds.
            db.medicationIntakeQueries.clear()
            db.medicationQueries.clear()

            backup.userProfile?.let { dto ->
                db.userProfileQueries.upsert(
                    tamaName = dto.tamaName,
                    lastPeriodStart = LocalDate.fromEpochDays(dto.lastPeriodStartEpochDay.toInt()),
                    avgCycleLengthDays = dto.avgCycleLengthDays,
                    avgPeriodLengthDays = dto.avgPeriodLengthDays,
                    tryingToConceive = dto.tryingToConceive,
                    onContraception = dto.onContraception,
                    irregularCycles = dto.irregularCycles,
                    onboardingComplete = dto.onboardingComplete,
                    createdAt = Instant.fromEpochMilliseconds(dto.createdAtEpochMs),
                    dailyStepsGoal = dto.dailyStepsGoal,
                    sleepGoalMinutes = dto.sleepGoalMinutes,
                    waterGoalGlasses = dto.waterGoalGlasses,
                    // Not present in the (older) wire format — restore sensible defaults.
                    remindPeriodEnabled = true,
                    remindWaterEnabled = false,
                    waterReminderIntervalHours = 3,
                    remindPillsEnabled = true,
                )
            }

            backup.tamagotchi?.let { dto ->
                db.tamagotchiStateQueries.upsert(
                    lastMood = TamagotchiMood.valueOf(dto.lastMood),
                    hatched = dto.hatched,
                    accessoriesJson = dto.accessoriesJson,
                )
            }

            backup.cycleEntries.forEach { dto ->
                db.cycleEntryQueries.upsert(
                    id = dto.id,
                    startDate = LocalDate.fromEpochDays(dto.startDateEpochDay.toInt()),
                    endDate = dto.endDateEpochDay?.let { LocalDate.fromEpochDays(it.toInt()) },
                    lengthDays = dto.lengthDays,
                )
            }

            backup.periodDays.forEach { dto ->
                db.periodDayQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    flow = PeriodFlow.valueOf(dto.flow),
                )
            }

            backup.moods.forEach { dto ->
                db.moodLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    mood = Mood.valueOf(dto.mood),
                    energy = dto.energy,
                    notes = dto.notes,
                )
            }

            backup.water.forEach { dto ->
                db.waterLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    glasses = dto.glasses,
                    goal = dto.goal,
                )
            }

            backup.hormones.forEach { dto ->
                db.hormoneLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    hormone = dto.hormone,
                    value_ = dto.value,
                    unit = dto.unit,
                    notes = dto.notes,
                )
            }

            backup.workouts.forEach { dto ->
                db.workoutLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    type = WorkoutType.valueOf(dto.type),
                    durationMinutes = dto.durationMinutes,
                    intensity = WorkoutIntensity.valueOf(dto.intensity),
                    notes = dto.notes,
                )
            }

            backup.symptoms.forEach { dto ->
                db.symptomLogQueries.upsert(
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    symptom = Symptom.valueOf(dto.symptom),
                )
            }

            backup.cravings.forEach { dto ->
                db.cravingLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    craving = CravingHint.valueOf(dto.craving),
                )
            }

            backup.sleeps.forEach { dto ->
                db.sleepLogQueries.upsert(
                    id = dto.id,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    bedMinuteOfDay = dto.bedMinuteOfDay,
                    wakeMinuteOfDay = dto.wakeMinuteOfDay,
                    durationMinutes = dto.durationMinutes,
                    rating = SleepRating.valueOf(dto.rating),
                )
            }

            // Insert meds before intakes, mirroring the clear order in reverse.
            backup.medications.forEach { dto ->
                db.medicationQueries.upsert(
                    id = dto.id,
                    name = dto.name,
                    dosesPerDay = dto.dosesPerDay,
                    slotsMask = dto.slotsMask,
                    active = dto.active,
                    createdAt = Instant.fromEpochMilliseconds(dto.createdAtEpochMs),
                    frequencyKind = dto.frequencyKind,
                    frequencyValue = dto.frequencyValue,
                )
            }

            backup.medicationIntakes.forEach { dto ->
                db.medicationIntakeQueries.insertAllRow(
                    id = dto.id,
                    medicationId = dto.medicationId,
                    date = LocalDate.fromEpochDays(dto.dateEpochDay.toInt()),
                    slot = TimeOfDay.valueOf(dto.slot),
                )
            }
        }
    }

    companion object {
        val DEFAULT_JSON = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

        /**
         * Parse + validate a backup file with no DB dependency, so it can be
         * called before the user confirms a destructive restore (and unit
         * tested in isolation).
         */
        fun parse(text: String, json: Json = DEFAULT_JSON): TamatamiBackup {
            val backup = try {
                json.decodeFromString(TamatamiBackup.serializer(), text)
            } catch (e: Exception) {
                throw InvalidBackupException("Not a valid backup file: ${e.message}")
            }
            if (backup.type != BACKUP_TYPE) {
                throw InvalidBackupException("File is not a tamatami backup.")
            }
            if (backup.version > BACKUP_VERSION) {
                throw InvalidBackupException(
                    "Backup was made by a newer app version (${backup.version}). Update the app first."
                )
            }
            return backup
        }
    }
}
