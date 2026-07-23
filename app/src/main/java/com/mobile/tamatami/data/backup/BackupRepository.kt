package com.mobile.tamatami.data.backup

import androidx.room.withTransaction
import com.mobile.tamatami.data.db.TamatamiDatabase
import kotlinx.serialization.json.Json

/** Thrown when an imported file isn't a recognizable tamatami backup. */
class InvalidBackupException(message: String) : Exception(message)

/**
 * Full-database JSON backup and restore.
 *
 * [exportToJson] reads every table and serializes a single [TamatamiBackup]
 * envelope. [importFromJson] parses that envelope and, inside one Room
 * transaction, **replaces all data** — every table is cleared and then
 * repopulated from the file, so the DB ends up exactly matching the backup.
 *
 * Because it wipes existing rows, callers should confirm with the user first.
 */
class BackupRepository(
    private val db: TamatamiDatabase,
    private val json: Json = DEFAULT_JSON,
) {
    suspend fun exportToJson(exportedAtEpochMs: Long): String {
        val backup = TamatamiBackup(
            exportedAtEpochMs = exportedAtEpochMs,
            userProfile = db.userProfileDao().get()?.toDto(),
            tamagotchi = db.tamagotchiStateDao().get()?.toDto(),
            cycleEntries = db.cycleEntryDao().getAll().map { it.toDto() },
            periodDays = db.periodDayDao().getAll().map { it.toDto() },
            moods = db.moodLogDao().getAll().map { it.toDto() },
            water = db.waterLogDao().getAll().map { it.toDto() },
            hormones = db.hormoneLogDao().getAll().map { it.toDto() },
            workouts = db.workoutLogDao().getAll().map { it.toDto() },
            symptoms = db.symptomLogDao().getAll().map { it.toDto() },
            cravings = db.cravingLogDao().getAll().map { it.toDto() },
            sleeps = db.sleepLogDao().getAll().map { it.toDto() },
            medications = db.medicationDao().getAll().map { it.toDto() },
            medicationIntakes = db.medicationIntakeDao().getAll().map { it.toDto() },
        )
        return json.encodeToString(TamatamiBackup.serializer(), backup)
    }

    /** Parse + validate only, so the UI can confirm before touching the DB. */
    fun parse(text: String): TamatamiBackup = parse(text, json)

    suspend fun importFromJson(text: String) = restore(parse(text))

    /** Clear-then-insert every table atomically. */
    suspend fun restore(backup: TamatamiBackup) {
        db.withTransaction {
            // Clear everything first so the result exactly matches the file.
            db.userProfileDao().clear()
            db.tamagotchiStateDao().clear()
            db.cycleEntryDao().clear()
            db.periodDayDao().clear()
            db.moodLogDao().clear()
            db.waterLogDao().clear()
            db.hormoneLogDao().clear()
            db.workoutLogDao().clear()
            db.symptomLogDao().clear()
            db.cravingLogDao().clear()
            db.sleepLogDao().clear()
            // Intakes reference a medication id, so clear them before the meds.
            db.medicationIntakeDao().clear()
            db.medicationDao().clear()

            backup.userProfile?.let { db.userProfileDao().upsert(it.toEntity()) }
            backup.tamagotchi?.let { db.tamagotchiStateDao().upsert(it.toEntity()) }
            db.cycleEntryDao().insertAll(backup.cycleEntries.map { it.toEntity() })
            db.periodDayDao().insertAll(backup.periodDays.map { it.toEntity() })
            db.moodLogDao().insertAll(backup.moods.map { it.toEntity() })
            db.waterLogDao().insertAll(backup.water.map { it.toEntity() })
            db.hormoneLogDao().insertAll(backup.hormones.map { it.toEntity() })
            db.workoutLogDao().insertAll(backup.workouts.map { it.toEntity() })
            db.symptomLogDao().insertAll(backup.symptoms.map { it.toEntity() })
            db.cravingLogDao().insertAll(backup.cravings.map { it.toEntity() })
            db.sleepLogDao().insertAll(backup.sleeps.map { it.toEntity() })
            // Insert meds before intakes, mirroring the clear order in reverse.
            db.medicationDao().insertAll(backup.medications.map { it.toEntity() })
            db.medicationIntakeDao().insertAll(backup.medicationIntakes.map { it.toEntity() })
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
