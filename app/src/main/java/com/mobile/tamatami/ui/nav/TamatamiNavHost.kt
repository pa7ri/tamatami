package com.mobile.tamatami.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.screens.calendar.CalendarScreen
import com.mobile.tamatami.ui.screens.cycleinfo.CycleInfoScreen
import com.mobile.tamatami.ui.screens.home.HomeScreen
import com.mobile.tamatami.ui.screens.hormones.HormonesScreen
import com.mobile.tamatami.ui.screens.nutrition.NutritionScreen
import com.mobile.tamatami.ui.screens.onboarding.onboardingGraph
import com.mobile.tamatami.ui.screens.settings.SettingsScreen
import com.mobile.tamatami.ui.screens.training.TrainingScreen

@Composable
fun TamatamiNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val profile by container.userRepository.observeProfile().collectAsState(initial = null)

    // Wait for the profile flow's first value so we don't briefly show
    // onboarding to a returning user before the DB read completes.
    var resolved by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        container.userRepository.getProfile()
        resolved = true
    }
    if (!resolved) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val onboardingDone = profile?.onboardingComplete == true
    val start: TamatamiRoute =
        if (onboardingDone) TamatamiRoute.Home else TamatamiRoute.OnboardingGraph

    NavHost(navController = navController, startDestination = start) {
        onboardingGraph(
            navController = navController,
            container = container,
            onComplete = {
                navController.navigate(TamatamiRoute.Home) {
                    popUpTo(TamatamiRoute.OnboardingGraph) { inclusive = true }
                }
            },
        )
        composable<TamatamiRoute.Home> { HomeScreen(navController, container) }
        composable<TamatamiRoute.Calendar> { CalendarScreen(navController, container) }
        composable<TamatamiRoute.Training> { TrainingScreen(navController, container) }
        composable<TamatamiRoute.Nutrition> { NutritionScreen(navController, container) }
        composable<TamatamiRoute.Hormones> { HormonesScreen(navController, container) }
        composable<TamatamiRoute.CycleInfo> { CycleInfoScreen(navController, container) }
        composable<TamatamiRoute.Settings> { SettingsScreen(navController, container) }
    }
}
