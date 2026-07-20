package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.CravingLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CravingLogDao {
    @Query("SELECT * FROM craving_log WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<CravingLogEntity?>

    @Query("SELECT * FROM craving_log ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 90): Flow<List<CravingLogEntity>>

    @Upsert
    suspend fun upsert(entity: CravingLogEntity)

    @Query("DELETE FROM craving_log WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM craving_log")
    suspend fun getAll(): List<CravingLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<CravingLogEntity>)

    @Query("DELETE FROM craving_log")
    suspend fun clear()
}
