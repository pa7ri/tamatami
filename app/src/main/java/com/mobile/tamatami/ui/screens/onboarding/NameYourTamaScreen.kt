package com.mobile.tamatami.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NameYourTamaScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val draft by viewModel.draft.collectAsState()
    OnboardingShell(
        title = "Name your Tama",
        subtitle = "Pick something silly. You can always change it later.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = onNext,
        primaryEnabled = draft.tamaName.isNotBlank(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OnboardingTamaPreview()
            androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = draft.tamaName,
                onValueChange = viewModel::setName,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                ),
                label = { Text("Name", color = Color.White) },
            )
        }
    }
}
