package com.mobile.tamatami.ui.screens.hormones

import com.mobile.tamatami.db.HormoneLog
import com.mobile.tamatami.domain.hormones.HormoneMarker
import kotlinx.datetime.LocalDate

data class HormonesUiState(
    val selectedMarker: HormoneMarker,
    val entries: List<HormoneLog>,
    val markerPoints: List<Pair<LocalDate, Float>>,
) {
    val entriesByDate: Map<LocalDate, List<HormoneLog>>
        get() = entries.groupBy { it.date }

    companion object {
        val Empty = HormonesUiState(
            selectedMarker = HormoneMarker.ESTROGEN,
            entries = emptyList(),
            markerPoints = emptyList(),
        )
    }
}
