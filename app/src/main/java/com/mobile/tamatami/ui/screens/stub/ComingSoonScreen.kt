package com.mobile.tamatami.ui.screens.stub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mobile.tamatami.ui.components.TamatamiBottomBar
import com.mobile.tamatami.ui.components.TamatamiScaffold

@Composable
fun ComingSoonScreen(
    title: String,
    body: String,
    navController: NavHostController,
) {
    TamatamiScaffold(
        title = title,
        bottomBar = { TamatamiBottomBar(navController) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card {
                Text(
                    text = body,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}
