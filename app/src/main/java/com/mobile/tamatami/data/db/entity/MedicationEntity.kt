package com.mobile.tamatami.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A configured medication the user wants to track. [slotsMask] is a bitmask of
 * [com.mobile.tamatami.domain.medication.TimeOfDay] ordinals (encode/decode via
 * [com.mobile.tamatami.domain.medication.MedicationSchedule]) — kept as an Int so
 * we avoid a list type-converter. Daily intake events live in
 * [MedicationIntakeEntity]; deactivating (rather than deleting) preserves history.
 */
@Entity(tableName = "medication")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosesPerDay: Int,
    val slotsMask: Int,
    val active: Boolean = true,
    val createdAt: Instant,
)
