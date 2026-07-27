package com.mobile.tamatami.data.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pure-JVM coverage of the backup format: the full JSON serialize/parse
 * round-trip over the primitive DTOs, and [BackupRepository.parse] validation.
 * The DB clear/insert wiring in `restore` runs against the shared SQLDelight
 * database (covered by the shared module's DB round-trip test); the DTO wire
 * format — everything with serialization risk — is exercised here.
 *
 * DTOs are built directly (they're plain @Serializable primitives) rather than
 * mapped from entities, since the data layer no longer exposes Room entities.
 */
class BackupMappingTest {

    private val json = BackupRepository.DEFAULT_JSON

    private fun sampleBackup(): TamatamiBackup {
        val dEpochDay = 20_653L // 2026-07-19 epoch day
        return TamatamiBackup(
            exportedAtEpochMs = 1_700_000_000_000L,
            userProfile = UserProfileDto(
                id = 0,
                tamaName = "Momo",
                lastPeriodStartEpochDay = 20_635L,
                avgCycleLengthDays = 28,
                avgPeriodLengthDays = 5,
                tryingToConceive = false,
                onContraception = true,
                irregularCycles = false,
                onboardingComplete = true,
                createdAtEpochMs = 1_600_000_000_000L,
            ),
            tamagotchi = TamagotchiStateDto(
                id = 0,
                lastMood = "HAPPY",
                hatched = true,
                accessoriesJson = "[\"hat\"]",
            ),
            cycleEntries = listOf(
                CycleEntryDto(1, 20_635L, 20_640L, 28),
                CycleEntryDto(2, 20_607L, null, null),
            ),
            periodDays = listOf(PeriodDayDto(1, dEpochDay, "HEAVY")),
            moods = listOf(MoodLogDto(1, dEpochDay, "GOOD", 4, "ok")),
            water = listOf(WaterLogDto(1, dEpochDay, 6, 8)),
            hormones = listOf(HormoneLogDto(1, dEpochDay, "LH", 12.5f, "mIU/mL", null)),
            workouts = listOf(
                WorkoutLogDto(1, dEpochDay, "YOGA", 30, "LOW", "am"),
                WorkoutLogDto(2, dEpochDay, "STRENGTH", 45, "HIGH", null),
            ),
            symptoms = listOf(SymptomLogDto(dEpochDay, "CRAMPS")),
            cravings = listOf(CravingLogDto(1, dEpochDay, "SWEET")),
            sleeps = listOf(SleepLogDto(1, dEpochDay, 1380, 420, 480, "RESTFUL")),
            medications = listOf(
                MedicationDto(1, "Iron", 1, 0b001, true, 1_650_000_000_000L),
                MedicationDto(2, "Vitamin D", 2, 0b101, false, 1_651_000_000_000L),
            ),
            medicationIntakes = listOf(
                MedicationIntakeDto(1, 1, dEpochDay, "MORNING"),
                MedicationIntakeDto(2, 2, dEpochDay, "EVENING"),
            ),
        )
    }

    @Test
    fun `full backup survives json round trip`() {
        val original = sampleBackup()
        val text = json.encodeToString(TamatamiBackup.serializer(), original)
        val parsed = json.decodeFromString(TamatamiBackup.serializer(), text)
        assertThat(parsed).isEqualTo(original)
        // Field-level spot check that enum names + order survive.
        assertThat(parsed.workouts.map { it.type })
            .containsExactly("YOGA", "STRENGTH").inOrder()
        assertThat(parsed.userProfile?.tamaName).isEqualTo("Momo")
        assertThat(parsed.medications.map { it.slotsMask }).containsExactly(0b001, 0b101).inOrder()
    }

    @Test
    fun `parse rejects non-tamatami json`() {
        try {
            BackupRepository.parse("""{"type":"something-else","version":1,"exportedAtEpochMs":0}""", json)
            error("expected InvalidBackupException")
        } catch (e: InvalidBackupException) {
            assertThat(e.message).contains("not a tamatami backup")
        }
    }

    @Test
    fun `parse rejects malformed json`() {
        try {
            BackupRepository.parse("not json at all", json)
            error("expected InvalidBackupException")
        } catch (e: InvalidBackupException) {
            assertThat(e.message).contains("valid backup")
        }
    }

    @Test
    fun `parse rejects newer version`() {
        val newer = """{"type":"$BACKUP_TYPE","version":${BACKUP_VERSION + 1},"exportedAtEpochMs":0}"""
        try {
            BackupRepository.parse(newer, json)
            error("expected InvalidBackupException")
        } catch (e: InvalidBackupException) {
            assertThat(e.message).contains("newer app version")
        }
    }
}
