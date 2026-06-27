package com.mobile.tamatami.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/** Type-safe Navigation Compose routes (Nav 2.8 @Serializable destinations). */
sealed interface TamatamiRoute {
    @Serializable data object OnboardingGraph : TamatamiRoute
    @Serializable data object Home : TamatamiRoute
    @Serializable data object Calendar : TamatamiRoute
    @Serializable data object Training : TamatamiRoute
    @Serializable data object CycleInfo : TamatamiRoute
    @Serializable data object Nutrition : TamatamiRoute
    @Serializable data object Hormones : TamatamiRoute
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
 * Bottom nav, in display order. Material 3 caps this at 5 items. Hormones is
 * reachable from Home's TopAppBar action and from a Cycle-info link row;
 * Settings is reachable from Home's TopAppBar action.
 */
val BottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(TamatamiRoute.Home, "Home", Icons.Outlined.Home),
    BottomNavItem(TamatamiRoute.Calendar, "Calendar", Icons.Outlined.CalendarMonth),
    BottomNavItem(TamatamiRoute.Training, "Training", Icons.Outlined.FitnessCenter),
    BottomNavItem(TamatamiRoute.CycleInfo, "Cycle", Icons.Outlined.Favorite),
    BottomNavItem(TamatamiRoute.Nutrition, "Nutrition", Icons.Outlined.LocalDining),
)
