package com.mobile.tamatami.data.db

import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * End-to-end DB round-trip on the JVM driver: proves the generated schema,
 * every column adapter (LocalDate/Instant/Int/enum), and the driver wiring all
 * work together — without Android or iOS. Validates the shared persistence
 * layer in this environment.
 */
class DatabaseRoundTripTest {

    private fun freshDb() = DatabaseFactory.create(DriverFactory())

    @Test
    fun userProfileRoundTrips() {
        val db = freshDb()
        val date = LocalDate(2026, 6, 1)
        val created = Instant.fromEpochMilliseconds(1_700_000_000_000)
        db.userProfileQueries.upsert(
            tamaName = "Tama",
            lastPeriodStart = date,
            avgCycleLengthDays = 28,
            avgPeriodLengthDays = 5,
            tryingToConceive = false,
            onContraception = false,
            irregularCycles = false,
            onboardingComplete = true,
            createdAt = created,
            dailyStepsGoal = 8000,
            sleepGoalMinutes = 480,
            waterGoalGlasses = 8,
            remindPeriodEnabled = true,
            remindWaterEnabled = false,
            waterReminderIntervalHours = 3,
            remindPillsEnabled = true,
        )
        val row = db.userProfileQueries.get().executeAsOne()
        assertEquals("Tama", row.tamaName)
        assertEquals(date, row.lastPeriodStart)          // LocalDate adapter
        assertEquals(created, row.createdAt)             // Instant adapter
        assertEquals(28, row.avgCycleLengthDays)         // Int adapter
        assertEquals(true, row.onboardingComplete)       // Boolean
    }

    @Test
    fun medicationAndIntakeEnumRoundTrip() {
        val db = freshDb()
        db.medicationQueries.insert(
            name = "Vit D",
            dosesPerDay = 1,
            slotsMask = 0b101,
            active = true,
            createdAt = Instant.fromEpochMilliseconds(0),
            frequencyKind = 0,
            frequencyValue = 0,
        )
        val medId = db.medicationQueries.lastInsertRowId().executeAsOne()
        val date = LocalDate(2026, 6, 2)
        db.medicationIntakeQueries.insert(medicationId = medId, date = date, slot = TimeOfDay.MORNING)

        val intakes = db.medicationIntakeQueries.observeByDate(date).executeAsList()
        assertEquals(1, intakes.size)
        assertEquals(TimeOfDay.MORNING, intakes.single().slot)  // enum adapter
        assertEquals(medId, intakes.single().medicationId)
    }

    @Test
    fun deleteRemovesRow() {
        val db = freshDb()
        val date = LocalDate(2026, 6, 3)
        db.waterLogQueries.insert(date = date, glasses = 4, goal = 8)
        assertEquals(4, db.waterLogQueries.observeByDate(date).executeAsOne().glasses)
        db.waterLogQueries.clear()
        assertNull(db.waterLogQueries.observeByDate(date).executeAsOneOrNull())
    }
}
