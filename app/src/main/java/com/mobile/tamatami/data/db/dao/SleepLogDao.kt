package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.SleepLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM sleep_log WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<SleepLogEntity?>

    /** Most recent nights, newest first — feeds the expected-quality prediction. */
    @Query("SELECT * FROM sleep_log ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 14): Flow<List<SleepLogEntity>>

    @Upsert
    suspend fun upsert(entity: SleepLogEntity)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM sleep_log")
    suspend fun getAll(): List<SleepLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<SleepLogEntity>)

    @Query("DELETE FROM sleep_log")
    suspend fun clear()
}
