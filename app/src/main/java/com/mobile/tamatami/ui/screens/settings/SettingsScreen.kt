package com.mobile.tamatami.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.settings.sections.DatePickerDialogCompat
import com.mobile.tamatami.ui.screens.settings.sections.NumberStepperRow
import com.mobile.tamatami.ui.screens.settings.sections.SettingsSectionCard
import com.mobile.tamatami.ui.screens.settings.sections.SwitchRow
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    container: AppContainer,
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(userRepository = container.userRepository),
    )
    val state by viewModel.state.collectAsState()
    var datePickerOpen by remember { mutableStateOf(false) }
    var savedAck by remember { mutableStateOf(false) }

    TamatamiScaffold(
        title = "Settings",
        bottomBar = { TamatamiBottomBar(navController) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // -- Profile -----------------------------------------------------
            SettingsSectionCard(title = "Profile") {
                OutlinedTextField(
                    value = state.tamaName,
                    onValueChange = viewModel::setTamaName,
                    label = { Text("Tama's name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // -- Cycle -------------------------------------------------------
            SettingsSectionCard(title = "Cycle") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Last period start",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            state.lastPeriodStart.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(onClick = { datePickerOpen = true }) { Text("Change") }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                NumberStepperRow(
                    label = "Average cycle length",
                    suffix = "days",
                    value = state.avgCycleLengthDays,
                    range = MIN_CYCLE..MAX_CYCLE,
                    onChange = viewModel::setCycleLength,
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                NumberStepperRow(
                    label = "Average period length",
                    suffix = "days",
                    value = state.avgPeriodLengthDays,
                    range = MIN_PERIOD..MAX_PERIOD,
                    onChange = viewModel::setPeriodLength,
                )
            }

            // -- Preferences -------------------------------------------------
            SettingsSectionCard(title = "Preferences") {
                SwitchRow(
                    label = "I'm trying to conceive",
                    checked = state.tryingToConceive,
                    onChange = viewModel::setTryingToConceive,
                )
                SwitchRow(
                    label = "I'm on hormonal contraception",
                    checked = state.onContraception,
                    onChange = viewModel::setOnContraception,
                )
                SwitchRow(
                    label = "My cycles are irregular",
                    checked = state.irregularCycles,
                    onChange = viewModel::setIrregularCycles,
                )
            }

            // -- About -------------------------------------------------------
            SettingsSectionCard(title = "About") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Version", modifier = Modifier.weight(1f))
                    Text(
                        APP_VERSION,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // -- Save bar ----------------------------------------------------
            Button(
                onClick = {
                    savedAck = true
                    viewModel.save()
                },
                enabled = viewModel.isDirty,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (savedAck && !viewModel.isDirty) "Saved ✓" else "Save changes")
            }
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Back") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (datePickerOpen) {
        DatePickerDialogCompat(
            initial = state.lastPeriodStart,
            onDismiss = { datePickerOpen = false },
            onConfirm = { picked ->
                viewModel.setLastPeriod(picked)
                datePickerOpen = false
            },
        )
    }
}

// Reasonable clamps so the steppers can't produce useless values. Matches the
// onboarding screens' implicit assumptions about cycle/period length.
private const val MIN_CYCLE = 20
private const val MAX_CYCLE = 45
private const val MIN_PERIOD = 1
private const val MAX_PERIOD = 10

// Hard-coded for now — pulling versionName from BuildConfig requires
// buildConfig=true in build.gradle.kts, which the project hasn't enabled.
private const val APP_VERSION = "1.0"
