package com.mobile.tamatami.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mobile.tamatami.domain.model.CyclePhase

data class PhaseColorTokens(
    val menstrual: Color,
    val follicular: Color,
    val ovulatory: Color,
    val luteal: Color,
    val unknown: Color,
) {
    fun colorFor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.MENSTRUAL -> menstrual
        CyclePhase.FOLLICULAR -> follicular
        CyclePhase.OVULATORY -> ovulatory
        CyclePhase.LUTEAL -> luteal
        CyclePhase.UNKNOWN -> unknown
    }
}

fun phaseColorTokens(darkTheme: Boolean): PhaseColorTokens = PhaseColorTokens(
    menstrual = PhaseMenstrual,
    follicular = PhaseFollicular,
    ovulatory = PhaseOvulatory,
    luteal = PhaseLuteal,
    unknown = if (darkTheme) Color(0xFF6B5867) else PhaseUnknown,
)

val LocalPhaseColors = staticCompositionLocalOf {
    phaseColorTokens(darkTheme = false)
}
