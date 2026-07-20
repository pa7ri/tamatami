package com.mobile.tamatami.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

/**
 * Deep, cool backdrop for the Tamagotchi hero card. Kept dark and desaturated
 * so the grey cat and white caption read clearly (the bright [phaseBrush]
 * washed the grey out). The current phase tints the top stop as a subtle
 * accent while the bottom stays a near-charcoal slate.
 */
private val HeroBase = Color(0xFF241B2E)   // deep plum-charcoal
fun heroBackdrop(phase: CyclePhase): Brush {
    val accent = when (phase) {
        CyclePhase.MENSTRUAL -> Color(0xFF7A2E4A)
        CyclePhase.FOLLICULAR -> Color(0xFF6E5A2E)
        CyclePhase.OVULATORY -> Color(0xFF7A4A2E)
        CyclePhase.LUTEAL -> Color(0xFF3E2E6E)
        CyclePhase.UNKNOWN -> Color(0xFF4A3A5A)
    }
    return Brush.linearGradient(listOf(accent, HeroBase))
}
