package com.mobile.tamatami.ui.theme

import androidx.compose.ui.graphics.Brush
import com.mobile.tamatami.domain.model.CyclePhase

/** Full 5-stop Instagram-style hero gradient. */
val InstagramBrush: Brush = Brush.linearGradient(
    colors = listOf(IgYellow, IgOrange, IgPink, IgMagenta, IgBlue)
)

/** A softer 2-stop blend tied to the user's current cycle phase. */
fun phaseBrush(phase: CyclePhase): Brush = when (phase) {
    CyclePhase.MENSTRUAL -> Brush.linearGradient(listOf(IgPink, PhaseMenstrual))
    CyclePhase.FOLLICULAR -> Brush.linearGradient(listOf(IgYellow, IgOrange))
    CyclePhase.OVULATORY -> Brush.linearGradient(listOf(IgOrange, IgPink))
    CyclePhase.LUTEAL -> Brush.linearGradient(listOf(IgMagenta, IgBlue))
    CyclePhase.UNKNOWN -> Brush.linearGradient(listOf(PhaseUnknown, IgMagenta))
}
