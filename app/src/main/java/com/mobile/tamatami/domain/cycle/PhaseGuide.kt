package com.mobile.tamatami.domain.cycle

import com.mobile.tamatami.domain.model.CyclePhase

data class PhaseGuide(
    val phase: CyclePhase,
    val headline: String,
    val body: String,
    val energyExpectation: String,
    val bodyChanges: List<String>,
    val moodTendency: String,
    val practicalTips: List<String>,
    val tamaNote: String,
    val sampleDayRange: String,
)

/** Static lookup. No tests needed — pure data. */
fun guideFor(phase: CyclePhase): PhaseGuide = when (phase) {
    CyclePhase.MENSTRUAL -> PhaseGuide(
        phase = phase,
        headline = "Resting & releasing",
        body = "Your period has arrived. Estrogen and progesterone are at their lowest, " +
            "which is what makes you feel slower — that's the design, not a failure. " +
            "Day one is officially the start of a new cycle.",
        energyExpectation = "Low on days 1–3, climbing back by day 4 or 5.",
        bodyChanges = listOf(
            "Uterine lining sheds — expect cramps, especially day 1–2",
            "Lower body temperature; you may feel cold easily",
            "Iron levels can dip; fatigue is common",
            "Bloating typically eases by day 3",
        ),
        moodTendency = "Inward, reflective, sometimes tearful. Honest self-talk runs strong.",
        practicalTips = listOf(
            "Front-load iron-rich foods (red meat, lentils, spinach + vit C)",
            "Hydrate well — period flow plus low estrogen = thirstier than usual",
            "Heat helps cramps more than NSAIDs alone",
            "Gentle movement (walking, yoga) eases symptoms; intense sessions can wait",
        ),
        tamaNote = "Your Tama is curled up under a blanket today.",
        sampleDayRange = "Cycle days 1–5",
    )
    CyclePhase.FOLLICULAR -> PhaseGuide(
        phase = phase,
        headline = "Building & rising",
        body = "Estrogen is climbing fast. Follicles in your ovaries are maturing, the " +
            "uterine lining is thickening, and you'll likely feel sharper, lighter, " +
            "and more social as the days pass.",
        energyExpectation = "Steady climb; usually highest energy of the cycle by the end of this phase.",
        bodyChanges = listOf(
            "Estrogen rising → glowing skin, better mood",
            "Cervical mucus becomes clearer and stretchier",
            "Resting heart rate often a few bpm lower than luteal",
            "Insulin sensitivity is good — carbs are well tolerated",
        ),
        moodTendency = "Optimistic, curious, ready for new projects.",
        practicalTips = listOf(
            "Great window to start new habits or programs",
            "Schedule the harder conversations or creative work here",
            "Build progressive strength — your body responds well to overload",
            "Cravings tend to be milder; less effort needed for nutrition",
        ),
        tamaNote = "Your Tama is bouncing around with new ideas.",
        sampleDayRange = "Cycle days 6 to ~13",
    )
    CyclePhase.OVULATORY -> PhaseGuide(
        phase = phase,
        headline = "Peak window",
        body = "An LH surge releases an egg from one of your ovaries. This phase is short " +
            "(2–3 days) but you'll often feel it: communication is easy, libido peaks, " +
            "energy is at its highest.",
        energyExpectation = "Peak. Best window for max-effort work, both physical and social.",
        bodyChanges = listOf(
            "Body temperature rises ~0.3°C after ovulation",
            "Brief mid-cycle pain (mittelschmerz) is normal",
            "Skin may be its clearest of the month",
            "Cervical mucus is at its most fertile-friendly",
        ),
        moodTendency = "Confident, extroverted, articulate.",
        practicalTips = listOf(
            "Lean into max-effort training — PRs land best here",
            "Front-load important presentations or negotiations",
            "Antioxidant-rich foods (berries, leafy greens) support the egg's release",
            "Track cervical mucus + basal temp if you're tracking fertility",
        ),
        tamaNote = "Your Tama is glowing — literally.",
        sampleDayRange = "Cycle days ~13–15 (varies by cycle length)",
    )
    CyclePhase.LUTEAL -> PhaseGuide(
        phase = phase,
        headline = "Slowing & sealing",
        body = "Progesterone takes over after ovulation, prepping the uterus in case of " +
            "implantation. Energy gently tapers; focus narrows. The last 3–5 days can " +
            "bring PMS if it shows up for you.",
        energyExpectation = "Moderate at the start, declining toward the end. PMS risk in the final 3–5 days.",
        bodyChanges = listOf(
            "Progesterone-driven warmth (BBT stays elevated)",
            "Slight water retention; bloating can return",
            "Skin sensitivity rises; breakouts possible late luteal",
            "Insulin sensitivity drops — carbs need more pairing",
        ),
        moodTendency = "Focused early, more irritable or weepy late. Inner critic gets louder.",
        practicalTips = listOf(
            "Front-load detail-oriented work in the first half",
            "Magnesium-rich foods (dark chocolate, pumpkin seeds) help PMS",
            "Steady-state cardio + moderate strength beats max effort",
            "Plan recovery; sleep matters more than usual",
        ),
        tamaNote = "Your Tama is wearing socks and reading by the window.",
        sampleDayRange = "Cycle days ~15 to start of next period",
    )
    CyclePhase.UNKNOWN -> PhaseGuide(
        phase = phase,
        headline = "Cycle pending",
        body = "Log your last period in Settings to unlock phase-aware guides.",
        energyExpectation = "—",
        bodyChanges = emptyList(),
        moodTendency = "—",
        practicalTips = emptyList(),
        tamaNote = "Your Tama is patiently waiting.",
        sampleDayRange = "—",
    )
}
