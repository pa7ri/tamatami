package com.mobile.tamatami.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mobile.tamatami.data.db.entity.TamagotchiStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TamagotchiStateDao {
    @Query("SELECT * FROM tamagotchi_state WHERE id = 0 LIMIT 1")
    fun observe(): Flow<TamagotchiStateEntity?>

    @Upsert
    suspend fun upsert(entity: TamagotchiStateEntity)
}
