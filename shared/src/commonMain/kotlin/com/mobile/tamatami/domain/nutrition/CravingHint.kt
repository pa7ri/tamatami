package com.mobile.tamatami.domain.nutrition

import com.mobile.tamatami.domain.model.CyclePhase

enum class CravingHint(val label: String) {
    SWEET("Sweet"),
    SALTY("Salty"),
    CHOCOLATE("Chocolate"),
    CARB("Carby"),
    MEAT("Meat / heavy"),
}

/**
 * Craving → curated alternatives, lightly tuned by phase. Idea isn't to scold
 * the user, but to swap a refined version for a nutrient-rich version that
 * answers the same urge.
 */
fun cravingSuggestions(craving: CravingHint, phase: CyclePhase): List<FoodSuggestion> = when (craving) {
    CravingHint.CHOCOLATE -> listOf(
        FoodSuggestion("Dark chocolate (70%+)", "The real thing — magnesium-rich"),
        FoodSuggestion("Cacao + banana smoothie", "Sweet + magnesium combo"),
        FoodSuggestion("Date stuffed with almond butter", "Like a candy bar, minus the spike"),
    ) + if (phase == CyclePhase.LUTEAL) listOf(
        FoodSuggestion("Pumpkin seeds (small handful)", "More magnesium for late luteal"),
    ) else emptyList()

    CravingHint.SWEET -> listOf(
        FoodSuggestion("Frozen berries + Greek yogurt", "Fast, sweet, protein-paired"),
        FoodSuggestion("Apple + nut butter", "Steady blood sugar"),
        FoodSuggestion("Dates + walnuts", "Caramel-ish, fibre-rich"),
    )

    CravingHint.SALTY -> listOf(
        FoodSuggestion("Avocado + sea salt on toast", "Potassium for fluid balance"),
        FoodSuggestion("Olives + cheese", "Salt that comes with fat & protein"),
        FoodSuggestion("Banana with salted peanut butter", "Potassium pairs with salt"),
        FoodSuggestion("Glass of water first", "Salt cravings often = thirst"),
    )

    CravingHint.CARB -> listOf(
        FoodSuggestion("Oats with cinnamon", "Slow carbs, steady energy"),
        FoodSuggestion("Sweet potato + butter", "Comforting without the crash"),
        FoodSuggestion("Sourdough toast + eggs", "Complex carb + protein"),
    ) + if (phase == CyclePhase.LUTEAL) listOf(
        FoodSuggestion("Quinoa bowl", "Higher protein carb option for luteal"),
    ) else emptyList()

    CravingHint.MEAT -> listOf(
        FoodSuggestion("Beef or bison burger", "Heme iron, especially helpful menstruating"),
        FoodSuggestion("Lamb stew", "Iron + B12"),
        FoodSuggestion("Lentil dahl + spinach", "Plant iron + Vit C for absorption"),
    ) + if (phase == CyclePhase.MENSTRUAL) listOf(
        FoodSuggestion("Liver paté on toast", "Densest source of heme iron"),
    ) else emptyList()
}
