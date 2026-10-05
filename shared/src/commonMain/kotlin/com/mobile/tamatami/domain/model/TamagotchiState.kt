package com.mobile.tamatami.domain.model

data class TamagotchiState(
    val mood: TamagotchiMood,
    val accessories: Set<Accessory>,
    val bounceHz: Float,
    val expression: TamaExpression = TamaExpression.GREETING,
) {
    companion object {
        val Idle = TamagotchiState(
            mood = TamagotchiMood.NEUTRAL,
            accessories = emptySet(),
            bounceHz = 0.6f,
            expression = TamaExpression.GREETING,
        )
    }
}
