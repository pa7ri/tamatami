package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobile.tamatami.data.db.entity.MedicationIntakeEntity
import com.mobile.tamatami.domain.medication.TimeOfDay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicationIntakeDao {
    @Query("SELECT * FROM medication_intake WHERE date = :date")
    fun observeByDate(date: LocalDate): Flow<List<MedicationIntakeEntity>>

    /** Non-reactive read for the reminder workers. */
    @Query("SELECT * FROM medication_intake WHERE date = :date")
    suspend fun getByDate(date: LocalDate): List<MedicationIntakeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: MedicationIntakeEntity)

    @Query("DELETE FROM medication_intake WHERE medicationId = :medicationId AND date = :date AND slot = :slot")
    suspend fun delete(medicationId: Long, date: LocalDate, slot: TimeOfDay)

    /** Remove all intake rows for a medication (called when a med is deleted). */
    @Query("DELETE FROM medication_intake WHERE medicationId = :medicationId")
    suspend fun deleteForMedication(medicationId: Long)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM medication_intake")
    suspend fun getAll(): List<MedicationIntakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<MedicationIntakeEntity>)

    @Query("DELETE FROM medication_intake")
    suspend fun clear()
}
