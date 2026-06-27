package com.mobile.tamatami.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mobile.tamatami.domain.model.TamagotchiMood

data class MoodColorTokens(
    val glowing: Color,
    val happy: Color,
    val content: Color,
    val neutral: Color,
    val sad: Color,
) {
    fun colorFor(mood: TamagotchiMood): Color = when (mood) {
        TamagotchiMood.GLOWING -> glowing
        TamagotchiMood.HAPPY -> happy
        TamagotchiMood.CONTENT -> content
        TamagotchiMood.NEUTRAL -> neutral
        TamagotchiMood.SAD -> sad
    }

    /** Slightly darker companion used for radial gradient shading. */
    fun shadeFor(mood: TamagotchiMood): Color = when (mood) {
        TamagotchiMood.GLOWING -> Color(0xFFCFA73C)
        TamagotchiMood.HAPPY -> Color(0xFFB85700)
        TamagotchiMood.CONTENT -> Color(0xFF8F1A53)
        TamagotchiMood.NEUTRAL -> Color(0xFF6F5670)
        TamagotchiMood.SAD -> Color(0xFF3F3057)
    }
}

fun moodColorTokens(darkTheme: Boolean): MoodColorTokens = MoodColorTokens(
    glowing = MoodGlowing,
    happy = MoodHappy,
    content = MoodContent,
    neutral = MoodNeutral,
    sad = if (darkTheme) Color(0xFF8A77A6) else MoodSad,
)

val LocalMoodColors = staticCompositionLocalOf {
    moodColorTokens(darkTheme = false)
}
