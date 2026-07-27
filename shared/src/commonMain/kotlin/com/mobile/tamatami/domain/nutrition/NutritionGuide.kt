package com.mobile.tamatami.domain.nutrition

import com.mobile.tamatami.domain.model.CyclePhase

data class FoodSuggestion(val name: String, val why: String)

data class PhaseNutrition(
    val phase: CyclePhase,
    val macroEmphasis: String,
    val keyMicronutrients: List<String>,
    val suggestedFoods: List<FoodSuggestion>,
    val foodsToLimit: List<String>,
)

/** Phase-aware nutrition lookups. Pure data; no tests. */
fun nutritionFor(phase: CyclePhase): PhaseNutrition = when (phase) {
    CyclePhase.MENSTRUAL -> PhaseNutrition(
        phase = phase,
        macroEmphasis = "Higher iron, warm cooked foods, gentle complex carbs.",
        keyMicronutrients = listOf("Iron", "Vitamin C", "B12", "Omega-3"),
        suggestedFoods = listOf(
            FoodSuggestion("Beef or lentil stew", "Replaces iron lost during menses"),
            FoodSuggestion("Spinach + citrus salad", "Vitamin C boosts iron absorption"),
            FoodSuggestion("Bone broth or miso soup", "Warming, easy on a tender gut"),
            FoodSuggestion("Salmon or sardines", "Omega-3 helps with cramps"),
            FoodSuggestion("Oats with cinnamon", "Steady carbs, anti-inflammatory"),
        ),
        foodsToLimit = listOf("Heavy caffeine", "Very cold drinks", "High-salt processed food"),
    )
    CyclePhase.FOLLICULAR -> PhaseNutrition(
        phase = phase,
        macroEmphasis = "Lighter, fresher foods; well-tolerated carbs.",
        keyMicronutrients = listOf("B-complex", "Vitamin E", "Probiotics", "Zinc"),
        suggestedFoods = listOf(
            FoodSuggestion("Fermented foods (kimchi, kefir)", "Supports estrogen metabolism"),
            FoodSuggestion("Sprouted grains", "Easy energy without a crash"),
            FoodSuggestion("Avocado on sourdough", "Healthy fats + complex carbs"),
            FoodSuggestion("Eggs", "Choline + protein for the building phase"),
            FoodSuggestion("Pumpkin seeds", "Zinc for follicle development"),
        ),
        foodsToLimit = listOf("Excess alcohol (slows estrogen clearance)"),
    )
    CyclePhase.OVULATORY -> PhaseNutrition(
        phase = phase,
        macroEmphasis = "Antioxidant-rich, fiber-forward to support estrogen clearance.",
        keyMicronutrients = listOf("Vitamin C", "Glutathione precursors", "Fiber", "Magnesium"),
        suggestedFoods = listOf(
            FoodSuggestion("Berries (blueberries, raspberries)", "Antioxidants for the egg"),
            FoodSuggestion("Cruciferous veg (broccoli, brussels)", "Support estrogen clearance"),
            FoodSuggestion("Quinoa bowls", "Complete protein + fiber"),
            FoodSuggestion("Citrus + leafy greens", "Vitamin C in the peak window"),
            FoodSuggestion("Brazil nuts (1–2/day)", "Selenium supports thyroid"),
        ),
        foodsToLimit = listOf("Refined sugar", "Heavily processed snacks"),
    )
    CyclePhase.LUTEAL -> PhaseNutrition(
        phase = phase,
        macroEmphasis = "More protein and fat; complex carbs over refined.",
        keyMicronutrients = listOf("Magnesium", "B6", "Calcium", "Tryptophan"),
        suggestedFoods = listOf(
            FoodSuggestion("Dark chocolate (70%+)", "Magnesium eases late-luteal mood dips"),
            FoodSuggestion("Sweet potato", "Slow carbs steady blood sugar"),
            FoodSuggestion("Pumpkin seeds", "Magnesium + zinc"),
            FoodSuggestion("Turkey or tofu", "Tryptophan supports calm sleep"),
            FoodSuggestion("Bananas", "B6, potassium for water balance"),
        ),
        foodsToLimit = listOf("Alcohol (worsens PMS)", "Refined sugar spikes", "Salty processed snacks"),
    )
    CyclePhase.UNKNOWN -> PhaseNutrition(
        phase = phase,
        macroEmphasis = "Eat balanced meals; log your cycle to unlock phase-specific tips.",
        keyMicronutrients = emptyList(),
        suggestedFoods = emptyList(),
        foodsToLimit = emptyList(),
    )
}
