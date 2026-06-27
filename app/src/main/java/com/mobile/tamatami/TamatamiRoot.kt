package com.mobile.tamatami

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.di.LocalAppContainer
import com.mobile.tamatami.ui.nav.TamatamiNavHost
import com.mobile.tamatami.ui.theme.TamatamiTheme

@Composable
fun TamatamiRoot(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        TamatamiTheme {
            TamatamiNavHost(container)
        }
    }
}
