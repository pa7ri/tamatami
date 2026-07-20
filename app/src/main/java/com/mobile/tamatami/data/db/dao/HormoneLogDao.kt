package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.HormoneLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HormoneLogDao {
    @Query("SELECT * FROM hormone_log WHERE date = :date ORDER BY id")
    fun observeByDate(date: LocalDate): Flow<List<HormoneLogEntity>>

    @Query("SELECT * FROM hormone_log WHERE date >= :sinceDate ORDER BY date DESC, id DESC")
    fun observeRecent(sinceDate: LocalDate): Flow<List<HormoneLogEntity>>

    @Query("SELECT * FROM hormone_log WHERE hormone = :hormone AND date >= :sinceDate ORDER BY date ASC")
    fun observeByMarker(hormone: String, sinceDate: LocalDate): Flow<List<HormoneLogEntity>>

    @Upsert
    suspend fun upsert(entity: HormoneLogEntity)

    @Query("DELETE FROM hormone_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM hormone_log")
    suspend fun getAll(): List<HormoneLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<HormoneLogEntity>)

    @Query("DELETE FROM hormone_log")
    suspend fun clear()
}
