package com.mobile.tamatami.ui.screens.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.ui.screens.home.HomeUiState
import com.mobile.tamatami.ui.tamagotchi.TamagotchiAvatar
import com.mobile.tamatami.ui.theme.phaseBrush

@Composable
fun TamaHeroSection(state: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(phaseBrush(state.cycle.phase))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TamagotchiAvatar(
                state = state.tama,
                modifier = Modifier.size(180.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = moodCaption(state.tama.mood, state.tamaName),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun moodCaption(mood: TamagotchiMood, name: String): String = when (mood) {
    TamagotchiMood.GLOWING -> "$name is glowing today ✨"
    TamagotchiMood.HAPPY -> "$name is feeling great"
    TamagotchiMood.CONTENT -> "$name is doing fine"
    TamagotchiMood.NEUTRAL -> "$name could use some care"
    TamagotchiMood.SAD -> "$name is having a tough day"
}
