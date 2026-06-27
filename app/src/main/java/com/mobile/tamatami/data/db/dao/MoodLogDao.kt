package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.MoodLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_log WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<MoodLogEntity?>

    @Upsert
    suspend fun upsert(entity: MoodLogEntity)
}
