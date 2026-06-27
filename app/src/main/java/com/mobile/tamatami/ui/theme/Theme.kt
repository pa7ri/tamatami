package com.mobile.tamatami.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Root theme. Wraps Material 3 Expressive and exposes our extra semantic token
 * sets ([LocalPhaseColors], [LocalMoodColors]) via CompositionLocal so feature
 * screens can read them without prop-drilling.
 *
 * If the linked material3 alpha doesn't yet ship [MaterialExpressiveTheme],
 * delete `@OptIn(...)` + the `MaterialExpressiveTheme(...) { }` wrapper and
 * fall back to `MaterialTheme(...)`. The rest of the tree is unaffected.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TamatamiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) tamatamiDarkColorScheme() else tamatamiLightColorScheme()

    CompositionLocalProvider(
        LocalPhaseColors provides phaseColorTokens(darkTheme),
        LocalMoodColors provides moodColorTokens(darkTheme),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = TamatamiTypography,
            shapes = TamatamiShapes,
            content = content,
        )
    }
}

/**
 * Non-expressive fallback that uses plain [MaterialTheme]. Kept around for
 * previews or for swapping in when the expressive API isn't available.
 */
@Composable
fun TamatamiClassicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) tamatamiDarkColorScheme() else tamatamiLightColorScheme()
    CompositionLocalProvider(
        LocalPhaseColors provides phaseColorTokens(darkTheme),
        LocalMoodColors provides moodColorTokens(darkTheme),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TamatamiTypography,
            shapes = TamatamiShapes,
            content = content,
        )
    }
}
