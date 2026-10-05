package com.mobile.tamatami.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mobile.tamatami.ui.nav.BottomNavItem
import com.mobile.tamatami.ui.nav.BottomNavItems
import com.mobile.tamatami.ui.nav.TamatamiRoute

@Composable
fun TamatamiBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    NavigationBar {
        BottomNavItems.forEach { item ->
            val selected = backStackEntry?.destination?.hierarchy?.any { dest ->
                dest.matches(item.route)
            } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            // Pop to the graph's start destination (saving its
                            // state) so switching tabs never stacks screens and
                            // each tab's state is preserved/restored — the
                            // canonical Compose bottom-nav pattern. Popping to a
                            // fixed route (Home) instead lost state for any tab
                            // that had a screen pushed on top of it.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

private fun androidx.navigation.NavDestination.matches(route: TamatamiRoute): Boolean {
    return when (route) {
        TamatamiRoute.Home -> hasRoute(TamatamiRoute.Home::class)
        TamatamiRoute.CycleInfo -> hasRoute(TamatamiRoute.CycleInfo::class)
        TamatamiRoute.Calendar -> hasRoute(TamatamiRoute.Calendar::class)
        TamatamiRoute.Health -> hasRoute(TamatamiRoute.Health::class)
        else -> false
    }
}

// Suppress unused param warning when items aren't passed in.
@Suppress("unused") private fun BottomNavItem.touch() = label
