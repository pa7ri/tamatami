package com.mobile.tamatami.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/** Type-safe Navigation Compose routes (Nav 2.8 @Serializable destinations). */
sealed interface TamatamiRoute {
    @Serializable data object OnboardingGraph : TamatamiRoute
    @Serializable data object Home : TamatamiRoute
    @Serializable data object CycleInfo : TamatamiRoute
    @Serializable data object Calendar : TamatamiRoute
    @Serializable data object Health : TamatamiRoute
    @Serializable data object Settings : TamatamiRoute

    // Onboarding step destinations -------------------------------------------
    @Serializable data object Welcome : TamatamiRoute
    @Serializable data object NameYourTama : TamatamiRoute
    @Serializable data object LastPeriod : TamatamiRoute
    @Serializable data object CycleLength : TamatamiRoute
    @Serializable data object PeriodLength : TamatamiRoute
    @Serializable data object Flags : TamatamiRoute
    @Serializable data object OnboardingSummary : TamatamiRoute
}

data class BottomNavItem(
    val route: TamatamiRoute,
    val label: String,
    val icon: ImageVector,
)

/**
 * Bottom nav, in display order — Material 3 caps this at 5 items.
 *
 * The Cycle tab ("Cycle & Training") hosts a Cycle-info / Nutrition / Training
 * sub-tab switch; the Health tab hosts a Hormones / Medication sub-tab switch.
 * Calendar is its own tab. Settings is a first-class tab.
 */
val BottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(TamatamiRoute.Home, "Home", Icons.Outlined.Home),
    BottomNavItem(TamatamiRoute.CycleInfo, "Cycle", Icons.Outlined.Favorite),
    BottomNavItem(TamatamiRoute.Calendar, "Calendar", Icons.Outlined.DateRange),
    BottomNavItem(TamatamiRoute.Health, "Health", Icons.Outlined.MonitorHeart),
    BottomNavItem(TamatamiRoute.Settings, "Settings", Icons.Outlined.Settings),
)
