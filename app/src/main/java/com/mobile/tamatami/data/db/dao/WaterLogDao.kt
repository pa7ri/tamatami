package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_log WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<WaterLogEntity?>

    @Upsert
    suspend fun upsert(entity: WaterLogEntity)
}
