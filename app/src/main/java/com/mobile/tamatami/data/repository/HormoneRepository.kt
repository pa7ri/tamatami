package com.mobile.tamatami.data.repository

import com.mobile.tamatami.data.db.dao.HormoneLogDao
import com.mobile.tamatami.data.db.entity.HormoneLogEntity
import com.mobile.tamatami.domain.hormones.HormoneMarker
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HormoneRepository(
    private val dao: HormoneLogDao,
) {
    fun observeRecent(sinceDate: LocalDate): Flow<List<HormoneLogEntity>> =
        dao.observeRecent(sinceDate)

    fun observeByMarker(marker: HormoneMarker, sinceDate: LocalDate): Flow<List<HormoneLogEntity>> =
        dao.observeByMarker(marker.storageKey, sinceDate)

    suspend fun upsert(
        id: Long = 0,
        date: LocalDate,
        marker: HormoneMarker,
        value: Float,
        unit: String = marker.defaultUnit,
        notes: String? = null,
    ) {
        dao.upsert(
            HormoneLogEntity(
                id = id,
                date = date,
                hormone = marker.storageKey,
                value = value,
                unit = unit,
                notes = notes,
            )
        )
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
