package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PeriodDayDao {
    @Query("SELECT * FROM period_day WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<PeriodDayEntity?>

    @Query("SELECT * FROM period_day ORDER BY date DESC LIMIT 90")
    fun observeRecent(): Flow<List<PeriodDayEntity>>

    @Upsert
    suspend fun upsert(entity: PeriodDayEntity)

    @Query("DELETE FROM period_day WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM period_day")
    suspend fun getAll(): List<PeriodDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<PeriodDayEntity>)

    @Query("DELETE FROM period_day")
    suspend fun clear()
}
