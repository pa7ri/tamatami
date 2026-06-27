package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.nav.TamatamiRoute

fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    container: AppContainer,
    onComplete: () -> Unit,
) {
    navigation<TamatamiRoute.OnboardingGraph>(startDestination = TamatamiRoute.Welcome) {
        composable<TamatamiRoute.Welcome> {
            WelcomeScreen(onNext = { navController.navigate(TamatamiRoute.NameYourTama) })
        }
        composable<TamatamiRoute.NameYourTama> {
            val vm = sharedOnboardingViewModel(navController, container)
            NameYourTamaScreen(
                viewModel = vm,
                onNext = { navController.navigate(TamatamiRoute.LastPeriod) },
                onBack = { navController.navigateUp() },
            )
        }
        composable<TamatamiRoute.LastPeriod> {
            val vm = sharedOnboardingViewModel(navController, container)
            LastPeriodScreen(
                viewModel = vm,
                onNext = { navController.navigate(TamatamiRoute.CycleLength) },
                onBack = { navController.navigateUp() },
            )
        }
        composable<TamatamiRoute.CycleLength> {
            val vm = sharedOnboardingViewModel(navController, container)
            CycleLengthScreen(
                viewModel = vm,
                onNext = { navController.navigate(TamatamiRoute.PeriodLength) },
                onBack = { navController.navigateUp() },
            )
        }
        composable<TamatamiRoute.PeriodLength> {
            val vm = sharedOnboardingViewModel(navController, container)
            PeriodLengthScreen(
                viewModel = vm,
                onNext = { navController.navigate(TamatamiRoute.Flags) },
                onBack = { navController.navigateUp() },
            )
        }
        composable<TamatamiRoute.Flags> {
            val vm = sharedOnboardingViewModel(navController, container)
            FlagsScreen(
                viewModel = vm,
                onNext = { navController.navigate(TamatamiRoute.OnboardingSummary) },
                onBack = { navController.navigateUp() },
            )
        }
        composable<TamatamiRoute.OnboardingSummary> {
            val vm = sharedOnboardingViewModel(navController, container)
            OnboardingSummaryScreen(
                viewModel = vm,
                onFinish = onComplete,
                onBack = { navController.navigateUp() },
            )
        }
    }
}

/**
 * Returns a ViewModel scoped to the [TamatamiRoute.OnboardingGraph] back-stack
 * entry so every onboarding step shares the same draft state.
 */
@Composable
private fun sharedOnboardingViewModel(
    navController: NavHostController,
    container: AppContainer,
): OnboardingViewModel {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(TamatamiRoute.OnboardingGraph)
    }
    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = OnboardingViewModel.Factory(
            userRepository = container.userRepository,
            cycleRepository = container.cycleRepository,
        ),
    )
}
