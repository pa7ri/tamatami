package com.mobile.tamatami.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mobile.tamatami.ui.screens.stub.ComingSoonScreen

@Composable
fun SettingsScreen(navController: NavHostController) {
    ComingSoonScreen(
        title = "Settings",
        body = "Profile, notifications, and data export options live here.",
        navController = navController,
    )
}
