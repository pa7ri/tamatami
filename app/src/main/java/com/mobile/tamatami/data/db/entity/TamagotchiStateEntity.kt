package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobile.tamatami.domain.model.TamagotchiMood

/** Last-known Tamagotchi mood, so cold start shows something sensible. */
@Entity(tableName = "tamagotchi_state")
data class TamagotchiStateEntity(
    @PrimaryKey val id: Int = 0,
    val lastMood: TamagotchiMood,
    val hatched: Boolean,
    val accessoriesJson: String,
)
