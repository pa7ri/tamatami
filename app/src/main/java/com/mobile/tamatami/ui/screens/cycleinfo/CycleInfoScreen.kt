package com.mobile.tamatami.ui.screens.cycleinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.domain.cycle.guideFor
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.nav.TamatamiRoute
import com.mobile.tamatami.ui.screens.home.displayName
import com.mobile.tamatami.ui.theme.phaseBrush

@Composable
fun CycleInfoScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: CycleInfoViewModel = viewModel(
        factory = CycleInfoViewModel.Factory(
            cycleRepository = container.cycleRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    val phases = listOf(
        CyclePhase.MENSTRUAL,
        CyclePhase.FOLLICULAR,
        CyclePhase.OVULATORY,
        CyclePhase.LUTEAL,
    )
    val selectedIndex = phases.indexOf(state.selectedPhase).coerceAtLeast(0)
    val guide = guideFor(state.selectedPhase)

    TamatamiScaffold(
        title = "Cycle info",
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(phaseBrush(state.selectedPhase))
                        .padding(24.dp),
                ) {
                    Column {
                        Text(
                            text = state.selectedPhase.displayName(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = guide.headline,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (state.selectedPhase == state.currentPhase &&
                            state.currentPhase != CyclePhase.UNKNOWN) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "You're on day ${state.cycle.cycleDay} of ${state.cycle.cycleLength}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 16.dp,
                ) {
                    phases.forEachIndexed { index, phase ->
                        Tab(
                            selected = index == selectedIndex,
                            onClick = { viewModel.selectPhase(phase) },
                            text = { Text(phase.displayName()) },
                        )
                    }
                }
            }
            item { Section("What's happening", guide.body) }
            item { Section("Energy expectation", guide.energyExpectation) }
            if (guide.bodyChanges.isNotEmpty()) {
                item { BulletSection("Body changes", guide.bodyChanges) }
            }
            item { Section("Mood tendency", guide.moodTendency) }
            if (guide.practicalTips.isNotEmpty()) {
                item { BulletSection("Practical tips", guide.practicalTips) }
            }
            item { Section("What your Tama feels", guide.tamaNote) }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    ListItem(
                        headlineContent = { Text("Log hormone readings") },
                        supportingContent = { Text("Track estradiol, progesterone, LH, FSH, TSH") },
                        leadingContent = {
                            Icon(Icons.Outlined.MonitorHeart, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(TamatamiRoute.Hormones)
                        },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BulletSection(title: String, bullets: List<String>) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            bullets.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodyMedium)
                    Text(item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
