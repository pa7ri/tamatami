package com.mobile.tamatami.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Instagram-inspired brand stops. These are the colors that anchor every
 * gradient and accent surface in the app.
 */
val IgYellow = Color(0xFFFEDA75)
val IgOrange = Color(0xFFFA7E1E)
val IgPink = Color(0xFFD62976)
val IgMagenta = Color(0xFF962FBF)
val IgBlue = Color(0xFF4F5BD5)

/** Cycle-phase semantic tokens. */
val PhaseMenstrual = Color(0xFFE4527A)
val PhaseFollicular = Color(0xFFFEDA75)
val PhaseOvulatory = Color(0xFFFA7E1E)
val PhaseLuteal = Color(0xFF962FBF)
val PhaseUnknown = Color(0xFFB58CB0)

/** Tamagotchi mood semantic tokens. */
val MoodGlowing = Color(0xFFFEDA75)
val MoodHappy = Color(0xFFFA7E1E)
val MoodContent = Color(0xFFD62976)
val MoodNeutral = Color(0xFFB58CB0)
val MoodSad = Color(0xFF6E5A8A)

// --- Light scheme ------------------------------------------------------------

private val PrimaryLight = IgMagenta
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFF6D6EC)
private val OnPrimaryContainerLight = Color(0xFF2A0A22)

private val SecondaryLight = IgOrange
private val OnSecondaryLight = Color(0xFF24160B)
private val SecondaryContainerLight = Color(0xFFFFE0CC)
private val OnSecondaryContainerLight = Color(0xFF2D1606)

private val TertiaryLight = IgBlue
private val OnTertiaryLight = Color(0xFFFFFFFF)
private val TertiaryContainerLight = Color(0xFFDCDFFF)
private val OnTertiaryContainerLight = Color(0xFF0A1146)

private val BackgroundLight = Color(0xFFFFF8FB)
private val OnBackgroundLight = Color(0xFF1C1118)
private val SurfaceLight = Color(0xFFFFF8FB)
private val OnSurfaceLight = Color(0xFF1C1118)
private val SurfaceVariantLight = Color(0xFFF4E6EE)
private val OnSurfaceVariantLight = Color(0xFF504349)
private val OutlineLight = Color(0xFFC9A5B8)

private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)

// --- Dark scheme -------------------------------------------------------------

private val PrimaryDark = Color(0xFFEBA9DD)
private val OnPrimaryDark = Color(0xFF421441)
private val PrimaryContainerDark = Color(0xFF6A2C66)
private val OnPrimaryContainerDark = Color(0xFFF8D8F0)

private val SecondaryDark = Color(0xFFFFB68B)
private val OnSecondaryDark = Color(0xFF4A2710)
private val SecondaryContainerDark = Color(0xFF683D23)
private val OnSecondaryContainerDark = Color(0xFFFFDBC7)

private val TertiaryDark = Color(0xFFBCC2FF)
private val OnTertiaryDark = Color(0xFF1B237A)
private val TertiaryContainerDark = Color(0xFF353D92)
private val OnTertiaryContainerDark = Color(0xFFDCDFFF)

private val BackgroundDark = Color(0xFF14080F)
private val OnBackgroundDark = Color(0xFFF5E1ED)
private val SurfaceDark = Color(0xFF14080F)
private val OnSurfaceDark = Color(0xFFF5E1ED)
private val SurfaceVariantDark = Color(0xFF504349)
private val OnSurfaceVariantDark = Color(0xFFD3C2C8)
private val OutlineDark = Color(0xFF9C8C93)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)

fun tamatamiLightColorScheme(): ColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = OnErrorLight,
)

fun tamatamiDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorDark,
    onError = OnErrorDark,
)
