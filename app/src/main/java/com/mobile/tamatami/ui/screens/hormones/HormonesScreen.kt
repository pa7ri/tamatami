package com.mobile.tamatami.ui.screens.hormones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.domain.hormones.HormoneMarker
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.hormones.sections.AddEntrySheet
import com.mobile.tamatami.ui.screens.hormones.sections.HormoneChart
import java.time.format.DateTimeFormatter

private val dateHeader: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
       androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HormonesScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: HormonesViewModel = viewModel(
        factory = HormonesViewModel.Factory(
            hormoneRepository = container.hormoneRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }
    val today = container.clock.today()

    TamatamiScaffold(
        title = "Hormones",
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${state.selectedMarker.displayName} trend (30 days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        HormoneChart(
                            marker = state.selectedMarker,
                            points = state.markerPoints,
                        )
                        Text(
                            "Reference: ${state.selectedMarker.expectedRange.start}–" +
                                "${state.selectedMarker.expectedRange.endInclusive} ${state.selectedMarker.defaultUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    HormoneMarker.entries.forEach { m ->
                        FilterChip(
                            selected = state.selectedMarker == m,
                            onClick = { viewModel.selectMarker(m) },
                            label = { Text(m.displayName) },
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { sheetOpen = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) { Text("Add reading") }
            }
            item {
                Text(
                    "Recent entries (last 90 days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (state.entries.isEmpty()) {
                item {
                    Text(
                        "Nothing logged yet. Tap \"Add reading\" above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                state.entriesByDate.toSortedMap(compareByDescending { it }).forEach { (date, entries) ->
                    stickyHeader {
                        Text(
                            text = date.format(dateHeader),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(entries.size, key = { entries[it].id }) { idx ->
                        val entry = entries[idx]
                        val marker = HormoneMarker.fromStorageKey(entry.hormone)
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = marker?.displayName ?: entry.hormone,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (!entry.notes.isNullOrBlank()) {
                                        Text(
                                            entry.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    text = "${entry.value} ${entry.unit}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (sheetOpen) {
        AddEntrySheet(
            today = today,
            initialMarker = state.selectedMarker,
            onDismiss = { sheetOpen = false },
            onSave = viewModel::saveEntry,
        )
    }
}
