package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mobile.tamatami.db.Medication
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.medication.MedicationSchedule
import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/** A medication plus the slots already taken for a given day. */
data class MedicationToday(
    val medication: Medication,
    val scheduledSlots: Set<TimeOfDay>,
    val takenSlots: Set<TimeOfDay>,
) {
    val adherence get() = MedicationSchedule.adherence(scheduledSlots, takenSlots)
}

/**
 * Pills tracker, backed by SQLDelight. Same public API as the old Room-based
 * repository; DAO calls are swapped for the generated `*Queries` and all dates
 * are kotlinx-datetime now.
 */
class MedicationRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Active medications joined with today's logged intakes, ready for the Pills
     * UI. Recombines whenever either the medication list or today's intakes change.
     */
    fun observeToday(date: LocalDate): Flow<List<MedicationToday>> =
        combine(
            db.medicationQueries.observeActive().asFlow().mapToList(dispatcher),
            db.medicationIntakeQueries.observeByDate(date).asFlow().mapToList(dispatcher),
        ) { meds, intakes ->
            meds.map { med ->
                MedicationToday(
                    medication = med,
                    scheduledSlots = MedicationSchedule.slotsOf(med.slotsMask),
                    takenSlots = intakes
                        .filter { it.medicationId == med.id }
                        .mapTo(mutableSetOf()) { it.slot },
                )
            }
        }

    /** Non-reactive read for the pill reminder worker. */
    suspend fun activeMedications(): List<Medication> =
        withContext(dispatcher) { db.medicationQueries.getAll().executeAsList().filter { it.active } }

    suspend fun takenSlotsFor(date: LocalDate, medicationId: Long): Set<TimeOfDay> =
        withContext(dispatcher) {
            db.medicationIntakeQueries.getByDate(date).executeAsList()
                .filter { it.medicationId == medicationId }
                .mapTo(mutableSetOf()) { it.slot }
        }

    suspend fun addMedication(name: String, dosesPerDay: Int, slots: Set<TimeOfDay>, now: Instant) {
        withContext(dispatcher) {
            db.medicationQueries.insert(
                name = name,
                dosesPerDay = dosesPerDay,
                slotsMask = MedicationSchedule.maskOf(slots),
                active = true,
                createdAt = now,
            )
        }
    }

    suspend fun updateMedication(medication: Medication) {
        withContext(dispatcher) {
            db.medicationQueries.upsert(
                id = medication.id,
                name = medication.name,
                dosesPerDay = medication.dosesPerDay,
                slotsMask = medication.slotsMask,
                active = medication.active,
                createdAt = medication.createdAt,
            )
        }
    }

    suspend fun deleteMedication(id: Long) {
        withContext(dispatcher) {
            db.medicationIntakeQueries.deleteForMedication(id)
            db.medicationQueries.deleteById(id)
        }
    }

    /** Toggle whether a given slot is logged as taken for [date]. */
    suspend fun setTaken(medicationId: Long, date: LocalDate, slot: TimeOfDay, taken: Boolean) {
        withContext(dispatcher) {
            if (taken) {
                db.medicationIntakeQueries.insert(medicationId = medicationId, date = date, slot = slot)
            } else {
                db.medicationIntakeQueries.delete(medicationId = medicationId, date = date, slot = slot)
            }
        }
    }
}
