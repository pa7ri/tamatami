package com.mobile.tamatami.data.backup

import com.google.common.truth.Truth.assertThat
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
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Pure-JVM coverage of the backup format: entity↔DTO mapping fidelity, the
 * full JSON serialize/parse round-trip, and [BackupRepository.parse]
 * validation. The DB clear/insert wiring in `restore` needs a real Room
 * instance (device/Robolectric) and is verified manually — everything with
 * actual serialization risk is exercised here.
 */
class BackupMappingTest {

    private val json = BackupRepository.DEFAULT_JSON

    private fun sampleBackup(): TamatamiBackup {
        val d = LocalDate.of(2026, 7, 19).toEpochDay()
        return TamatamiBackup(
            exportedAtEpochMs = 1_700_000_000_000L,
            userProfile = UserProfileEntity(
                id = 0,
                tamaName = "Momo",
                lastPeriodStart = LocalDate.of(2026, 7, 1),
                avgCycleLengthDays = 28,
                avgPeriodLengthDays = 5,
                tryingToConceive = false,
                onContraception = true,
                irregularCycles = false,
                onboardingComplete = true,
                createdAt = Instant.ofEpochMilli(1_600_000_000_000L),
            ).toDto(),
            tamagotchi = TamagotchiStateEntity(
                id = 0,
                lastMood = TamagotchiMood.HAPPY,
                hatched = true,
                accessoriesJson = "[\"hat\"]",
            ).toDto(),
            cycleEntries = listOf(
                CycleEntryEntity(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 6), 28).toDto(),
                CycleEntryEntity(2, LocalDate.of(2026, 6, 3), null, null).toDto(),
            ),
            periodDays = listOf(PeriodDayEntity(1, LocalDate.ofEpochDay(d), PeriodFlow.HEAVY).toDto()),
            moods = listOf(MoodLogEntity(1, LocalDate.ofEpochDay(d), Mood.GOOD, 4, "ok").toDto()),
            water = listOf(WaterLogEntity(1, LocalDate.ofEpochDay(d), 6, 8).toDto()),
            hormones = listOf(
                HormoneLogEntity(1, LocalDate.ofEpochDay(d), "LH", 12.5f, "mIU/mL", null).toDto(),
            ),
            workouts = listOf(
                WorkoutLogEntity(1, LocalDate.ofEpochDay(d), WorkoutType.YOGA, 30, WorkoutIntensity.LOW, "am").toDto(),
                WorkoutLogEntity(2, LocalDate.ofEpochDay(d), WorkoutType.STRENGTH, 45, WorkoutIntensity.HIGH, null).toDto(),
            ),
            symptoms = listOf(SymptomLogEntity(LocalDate.ofEpochDay(d), Symptom.entries.first()).toDto()),
            cravings = listOf(CravingLogEntity(1, LocalDate.ofEpochDay(d), CravingHint.entries.first()).toDto()),
        )
    }

    @Test
    fun `entity to dto to entity preserves every field`() {
        val date = LocalDate.of(2026, 7, 19)
        val workout = WorkoutLogEntity(7, date, WorkoutType.HIIT, 42, WorkoutIntensity.HIGH, "note")
        assertThat(workout.toDto().toEntity()).isEqualTo(workout)

        val profile = UserProfileEntity(
            0, "Tama", date, 30, 6, true, false, true, true, Instant.ofEpochMilli(123456789L),
        )
        assertThat(profile.toDto().toEntity()).isEqualTo(profile)

        val cycle = CycleEntryEntity(3, date, null, null)
        assertThat(cycle.toDto().toEntity()).isEqualTo(cycle)

        val symptom = SymptomLogEntity(date, Symptom.entries.first())
        assertThat(symptom.toDto().toEntity()).isEqualTo(symptom)
    }

    @Test
    fun `full backup survives json round trip`() {
        val original = sampleBackup()
        val text = json.encodeToString(TamatamiBackup.serializer(), original)
        val parsed = json.decodeFromString(TamatamiBackup.serializer(), text)
        assertThat(parsed).isEqualTo(original)
        // And the mapped-back entities match the workouts we put in.
        assertThat(parsed.workouts.map { it.toEntity().type })
            .containsExactly(WorkoutType.YOGA, WorkoutType.STRENGTH).inOrder()
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
