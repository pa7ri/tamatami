package com.mobile.tamatami.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mobile.tamatami.di.AppContainer
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.screens.home.sections.CycleStatusSection
import com.mobile.tamatami.ui.screens.home.sections.NextPeriodSection
import com.mobile.tamatami.ui.screens.home.sections.QuickLogSection
import com.mobile.tamatami.ui.screens.home.sections.TamaHeroSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    container: AppContainer,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            userRepository = container.userRepository,
            cycleRepository = container.cycleRepository,
            dailyRepository = container.dailyLogRepository,
            tamaRepository = container.tamagotchiRepository,
            clock = container.clock,
        )
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hi, ${state.tamaName} 👋") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = { TamatamiBottomBar(navController) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TamaHeroSection(state = state) }
            item { CycleStatusSection(state = state) }
            item { NextPeriodSection(state = state) }
            item {
                QuickLogSection(
                    state = state,
                    onAddWater = viewModel::addWater,
                    onRemoveWater = viewModel::removeWater,
                    onMoodSelected = viewModel::setMood,
                    onFlowSelected = viewModel::setFlow,
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
