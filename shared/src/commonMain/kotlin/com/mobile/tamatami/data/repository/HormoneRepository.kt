package com.mobile.tamatami.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mobile.tamatami.db.HormoneLog
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.domain.hormones.HormoneMarker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Hormone logs, backed by SQLDelight. Returns the generated [HormoneLog] row
 * (replacing the old Room `HormoneLogEntity`).
 */
class HormoneRepository(
    private val db: TamatamiDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    fun observeRecent(sinceDate: LocalDate): Flow<List<HormoneLog>> =
        db.hormoneLogQueries.observeRecent(sinceDate).asFlow().mapToList(dispatcher)

    fun observeByMarker(marker: HormoneMarker, sinceDate: LocalDate): Flow<List<HormoneLog>> =
        db.hormoneLogQueries.observeByMarker(marker.storageKey, sinceDate).asFlow().mapToList(dispatcher)

    suspend fun upsert(
        id: Long = 0,
        date: LocalDate,
        marker: HormoneMarker,
        value: Float,
        unit: String = marker.defaultUnit,
        notes: String? = null,
    ) {
        withContext(dispatcher) {
            if (id == 0L) {
                db.hormoneLogQueries.insert(
                    date = date,
                    hormone = marker.storageKey,
                    value_ = value,
                    unit = unit,
                    notes = notes,
                )
            } else {
                db.hormoneLogQueries.upsert(
                    id = id,
                    date = date,
                    hormone = marker.storageKey,
                    value_ = value,
                    unit = unit,
                    notes = notes,
                )
            }
        }
    }

    suspend fun delete(id: Long) {
        withContext(dispatcher) { db.hormoneLogQueries.deleteById(id) }
    }
}
