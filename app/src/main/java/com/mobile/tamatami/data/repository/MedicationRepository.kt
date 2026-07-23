package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.MedicationDao
import com.mobile.tamatami.data.db.dao.MedicationIntakeDao
import com.mobile.tamatami.data.db.entity.MedicationEntity
import com.mobile.tamatami.data.db.entity.MedicationIntakeEntity
import com.mobile.tamatami.domain.medication.MedicationSchedule
import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate

/** A medication plus the slots already taken for a given day. */
data class MedicationToday(
    val medication: MedicationEntity,
    val scheduledSlots: Set<TimeOfDay>,
    val takenSlots: Set<TimeOfDay>,
) {
    val adherence get() = MedicationSchedule.adherence(scheduledSlots, takenSlots)
}

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val intakeDao: MedicationIntakeDao,
) {
    /**
     * Active medications joined with today's logged intakes, ready for the Pills
     * UI. Recombines whenever either the medication list or today's intakes change.
     */
    fun observeToday(date: LocalDate): Flow<List<MedicationToday>> =
        combine(
            medicationDao.observeActive(),
            intakeDao.observeByDate(date),
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
    suspend fun activeMedications(): List<MedicationEntity> = medicationDao.getAll().filter { it.active }

    suspend fun takenSlotsFor(date: LocalDate, medicationId: Long): Set<TimeOfDay> =
        intakeDao.getByDate(date)
            .filter { it.medicationId == medicationId }
            .mapTo(mutableSetOf()) { it.slot }

    suspend fun addMedication(name: String, dosesPerDay: Int, slots: Set<TimeOfDay>, now: Instant) {
        medicationDao.upsert(
            MedicationEntity(
                name = name,
                dosesPerDay = dosesPerDay,
                slotsMask = MedicationSchedule.maskOf(slots),
                active = true,
                createdAt = now,
            )
        )
    }

    suspend fun updateMedication(medication: MedicationEntity) = medicationDao.upsert(medication)

    suspend fun deleteMedication(id: Long) {
        intakeDao.deleteForMedication(id)
        medicationDao.deleteById(id)
    }

    /** Toggle whether a given slot is logged as taken for [date]. */
    suspend fun setTaken(medicationId: Long, date: LocalDate, slot: TimeOfDay, taken: Boolean) {
        if (taken) {
            intakeDao.insert(MedicationIntakeEntity(medicationId = medicationId, date = date, slot = slot))
        } else {
            intakeDao.delete(medicationId, date, slot)
        }
    }
}
