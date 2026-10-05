package com.mobile.tamatami.ui.screens.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.SettingsAction
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.hormones.HormonesContent
import com.mobile.tamatami.ui.screens.pills.PillsContent

/**
 * The "Health" tab: a two-way sub-tab switch between hormone tracking and the
 * medication (pill) tracker. Hosts the single scaffold and bottom bar; each
 * sub-tab renders its own content composable with its own ViewModel.
 */
@Composable
fun HealthScreen(navController: NavHostController, container: AppContainer) {
    var subTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Hormones", "Medication")

    TamatamiScaffold(
        title = "Health",
        bottomBar = { TamatamiBottomBar(navController) },
        actions = { SettingsAction(navController) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = subTab) {
                subTabs.forEachIndexed { index, label ->
                    Tab(
                        selected = index == subTab,
                        onClick = { subTab = index },
                        text = { Text(label) },
                    )
                }
            }
            when (subTab) {
                0 -> HormonesContent(navController, container)
                else -> PillsContent(navController, container)
            }
        }
    }
}
