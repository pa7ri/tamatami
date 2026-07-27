package com.mobile.tamatami.domain.model

/**
 * Per-day symptoms the user can toggle on the calendar day card. Multiple
 * symptoms per date are allowed, so persistence uses a bridge table keyed
 * `(date, symptom)` rather than a wide column on a daily-log row.
 *
 * Ordering is the display order on the chips row — please keep clinically
 * common cycle-related symptoms first.
 */
enum class Symptom(val label: String) {
    CRAMPS("Cramps"),
    HEADACHE("Headache"),
    BLOATING("Bloating"),
    BACK_PAIN("Back pain"),
    TENDER_BREASTS("Tender breasts"),
    FATIGUE("Fatigue"),
    ACNE("Acne"),
    NAUSEA("Nausea"),
    MOOD_SWINGS("Mood swings"),
    INSOMNIA("Insomnia"),
}
