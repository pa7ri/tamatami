package com.mobile.tamatami.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mobile.tamatami.ui.nav.TamatamiRoute

/**
 * Gear action for the top app bar — navigates to the pushed Settings screen.
 * Settings is no longer a bottom-nav tab; every main screen surfaces it here
 * (Home, Cycle & Training, Calendar, Health). `launchSingleTop` avoids stacking
 * duplicate Settings entries if the gear is tapped repeatedly.
 */
@Composable
fun SettingsAction(navController: NavHostController) {
    IconButton(
        onClick = {
            navController.navigate(TamatamiRoute.Settings) { launchSingleTop = true }
        },
    ) {
        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
    }
}
