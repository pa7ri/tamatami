package com.mobile.tamatami.ui.screens.hormones

import com.mobile.tamatami.data.db.entity.HormoneLogEntity
import com.mobile.tamatami.domain.hormones.HormoneMarker
import java.time.LocalDate

data class HormonesUiState(
    val selectedMarker: HormoneMarker,
    val entries: List<HormoneLogEntity>,
    val markerPoints: List<Pair<LocalDate, Float>>,
) {
    val entriesByDate: Map<LocalDate, List<HormoneLogEntity>>
        get() = entries.groupBy { it.date }

    companion object {
        val Empty = HormonesUiState(
            selectedMarker = HormoneMarker.ESTROGEN,
            entries = emptyList(),
            markerPoints = emptyList(),
        )
    }
}
