package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastPeriodScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val draft by viewModel.draft.collectAsState()
    val initialMillis = draft.lastPeriodStart.atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
    val todayMillis = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= todayMillis
        },
    )

    LaunchedEffect(state) {
        snapshotFlow { state.selectedDateMillis }.collect { millis ->
            millis?.let {
                val date = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
                viewModel.setLastPeriod(date)
            }
        }
    }

    OnboardingShell(
        title = "When did your last period start?",
        subtitle = "We use this to predict your next one.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = onNext,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            DatePicker(
                state = state,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                ),
            )
        }
    }
}
