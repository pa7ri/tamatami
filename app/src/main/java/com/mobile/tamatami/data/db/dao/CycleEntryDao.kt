package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.CycleEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CycleEntryDao {
    @Query("SELECT * FROM cycle_entry ORDER BY startDate DESC")
    fun observeAll(): Flow<List<CycleEntryEntity>>

    @Query("SELECT * FROM cycle_entry ORDER BY startDate DESC LIMIT 1")
    suspend fun latest(): CycleEntryEntity?

    @Query("SELECT * FROM cycle_entry WHERE startDate = :date LIMIT 1")
    suspend fun byStart(date: LocalDate): CycleEntryEntity?

    @Upsert
    suspend fun upsert(entity: CycleEntryEntity)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM cycle_entry")
    suspend fun getAll(): List<CycleEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<CycleEntryEntity>)

    @Query("DELETE FROM cycle_entry")
    suspend fun clear()
}
