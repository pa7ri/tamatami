package com.mobile.tamatami.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.mobile.tamatami.domain.model.CyclePhase
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.nutrition.FoodSuggestion
import com.mobile.tamatami.domain.nutrition.cravingSuggestions
import com.mobile.tamatami.domain.nutrition.nutritionFor
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold
import com.mobile.tamatami.ui.screens.home.displayName
import com.mobile.tamatami.ui.theme.phaseBrush

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NutritionScreen(navController: NavHostController, container: AppContainer) {
    val viewModel: NutritionViewModel = viewModel(
        factory = NutritionViewModel.Factory(
            cycleRepository = container.cycleRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()
    val phaseInfo = nutritionFor(state.currentPhase)
    val suggestions: List<FoodSuggestion> = state.selectedCraving
        ?.let { cravingSuggestions(it, state.currentPhase) }
        ?: phaseInfo.suggestedFoods

    TamatamiScaffold(
        title = "Nutrition",
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
                        .clip(RoundedCornerShape(24.dp))
                        .background(phaseBrush(state.currentPhase))
                        .padding(20.dp),
                ) {
                    Column {
                        Text(
                            text = "${state.currentPhase.displayName()} phase",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = phaseInfo.macroEmphasis,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (phaseInfo.keyMicronutrients.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Focus: ${phaseInfo.keyMicronutrients.joinToString(", ")}",
                                color = Color.White.copy(alpha = 0.95f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Got a craving?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CravingHint.entries.forEach { c ->
                            FilterChip(
                                selected = state.selectedCraving == c,
                                onClick = { viewModel.toggleCraving(c) },
                                label = { Text(c.label) },
                            )
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = if (state.selectedCraving != null)
                            "Try these instead of refined ${state.selectedCraving!!.label.lowercase()}"
                        else "Suggested for this phase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            items(suggestions) { suggestion ->
                FoodCard(suggestion)
            }
            if (phaseInfo.foodsToLimit.isNotEmpty() && state.selectedCraving == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Worth limiting",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            phaseInfo.foodsToLimit.forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("•  ")
                                    Text(item, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FoodCard(suggestion: FoodSuggestion) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                suggestion.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                suggestion.why,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// `items` extension for LazyColumn — re-export to keep imports tidy in this file.
private fun androidx.compose.foundation.lazy.LazyListScope.items(
    items: List<FoodSuggestion>,
    itemContent: @Composable (FoodSuggestion) -> Unit,
) = items(items.size) { itemContent(items[it]) }
