package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication WHERE active = 1 ORDER BY createdAt")
    fun observeActive(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medication ORDER BY createdAt")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Upsert
    suspend fun upsert(entity: MedicationEntity): Long

    @Query("DELETE FROM medication WHERE id = :id")
    suspend fun deleteById(id: Long)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM medication")
    suspend fun getAll(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<MedicationEntity>)

    @Query("DELETE FROM medication")
    suspend fun clear()
}
