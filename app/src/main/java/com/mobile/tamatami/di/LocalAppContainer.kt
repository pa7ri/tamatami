package com.mobile.tamatami.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides the [AppContainer] to Composables without prop-drilling. Any
 * screen can call `LocalAppContainer.current.userRepository` directly.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided. Wrap the tree in CompositionLocalProvider(LocalAppContainer provides ...).")
}
