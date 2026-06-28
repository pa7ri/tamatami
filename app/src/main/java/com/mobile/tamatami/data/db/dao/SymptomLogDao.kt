package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.SymptomLogEntity
import com.mobile.tamatami.domain.model.Symptom
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SymptomLogDao {
    @Query("SELECT * FROM symptom_log WHERE date = :date")
    fun observeByDate(date: LocalDate): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_log ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 180): Flow<List<SymptomLogEntity>>

    @Upsert
    suspend fun upsert(entity: SymptomLogEntity)

    @Query("DELETE FROM symptom_log WHERE date = :date AND symptom = :symptom")
    suspend fun delete(date: LocalDate, symptom: Symptom)

    @Query("DELETE FROM symptom_log WHERE date = :date")
    suspend fun deleteAllForDate(date: LocalDate)
}
