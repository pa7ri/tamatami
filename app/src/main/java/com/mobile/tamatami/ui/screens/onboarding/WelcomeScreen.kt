package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.runtime.Composable

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    OnboardingShell(
        title = "Meet your Tama",
        subtitle = "A tiny companion that mirrors how your body and habits are doing today.",
        primaryLabel = "Let's begin",
        onPrimary = onNext,
    ) {
        OnboardingTamaPreview()
    }
}
