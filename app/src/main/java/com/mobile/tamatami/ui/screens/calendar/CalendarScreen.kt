package com.mobile.tamatami.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.calendar.sections.CalendarContent

/**
 * The "Calendar" tab. Wraps the embeddable [CalendarContent] (previously a
 * section at the bottom of Home) in the shared scaffold + bottom bar, with the
 * Settings gear in the top bar. [CalendarContent] owns no scroll of its own, so
 * the host provides a vertically-scrolling column.
 */
@Composable
fun CalendarScreen(navController: NavHostController, container: AppContainer) {
    TamatamiScaffold(
        title = "Calendar",
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            CalendarContent(container)
            Spacer(Modifier.height(24.dp))
        }
    }
}
