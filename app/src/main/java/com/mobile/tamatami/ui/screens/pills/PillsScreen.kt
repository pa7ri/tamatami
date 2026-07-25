package com.mobile.tamatami.ui.screens.pills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.pills.sections.AddMedicationSheet
import com.mobile.tamatami.ui.screens.pills.sections.MedicationCard

/**
 * Medication ("pill") tracker: add medications, see today's adherence, and tap
 * a slot chip to log it taken. Reached from the Cycle tab (alongside Hormones).
 * Screen chrome mirrors [com.mobile.tamatami.ui.screens.hormones.HormonesScreen].
 */
@Composable
fun PillsScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: PillsViewModel = viewModel(
        factory = PillsViewModel.Factory(
            medicationRepository = container.medicationRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    var sheetOpen by remember { mutableStateOf(false) }

    TamatamiScaffold(
        title = "Medication",
        bottomBar = { TamatamiBottomBar(navController) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
    ) { padding ->
        if (!state.loaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@TamatamiScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(
                    onClick = { sheetOpen = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) { Text("Add medication") }
            }

            if (state.meds.isEmpty()) {
                item {
                    Text(
                        "Nothing tracked yet. Tap \"Add medication\" to start — " +
                            "pill, birth control, or any daily supplement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(state.meds.size, key = { state.meds[it].medication.id }) { idx ->
                    val med = state.meds[idx]
                    MedicationCard(
                        med = med,
                        onToggle = { slot, taken ->
                            viewModel.toggleTaken(med.medication.id, slot, taken)
                        },
                        onDelete = { viewModel.deleteMedication(med.medication.id) },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (sheetOpen) {
        AddMedicationSheet(
            onDismiss = { sheetOpen = false },
            onSave = viewModel::addMedication,
        )
    }
}
