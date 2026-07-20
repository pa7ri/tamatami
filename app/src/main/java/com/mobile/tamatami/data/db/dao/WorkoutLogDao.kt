package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_log ORDER BY date DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int = 30): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_log WHERE date = :date ORDER BY id DESC")
    fun observeByDate(date: LocalDate): Flow<List<WorkoutLogEntity>>

    @Upsert
    suspend fun upsert(entity: WorkoutLogEntity)

    @Query("DELETE FROM workout_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    // -- Backup ---------------------------------------------------------------
    @Query("SELECT * FROM workout_log")
    suspend fun getAll(): List<WorkoutLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<WorkoutLogEntity>)

    @Query("DELETE FROM workout_log")
    suspend fun clear()
}
