import Foundation
import HealthKit
import Shared

/// A loggable activity: its display label + SF Symbol, the Apple Health
/// `HKWorkoutActivityType` it maps to, and the shared Kotlin `WorkoutType` it
/// persists as. `workoutType` must resolve to a real case of the shared enum —
/// keep these aligned with `WorkoutType.kt`.
struct Activity: Identifiable, Hashable {
    let id: String
    let label: String
    let symbol: String
    let healthKitType: HKWorkoutActivityType
    let workoutType: WorkoutType
}

enum ActivityCatalog {
    static let all: [Activity] = [
        Activity(id: "walk",       label: "Walk",       symbol: "figure.walk",                           healthKitType: .walking,                       workoutType: .walk),
        Activity(id: "run",        label: "Run",        symbol: "figure.run",                            healthKitType: .running,                       workoutType: .run),
        Activity(id: "hike",       label: "Hike",       symbol: "figure.hiking",                         healthKitType: .hiking,                        workoutType: .hike),
        Activity(id: "cycle",      label: "Cycle",      symbol: "figure.outdoor.cycle",                  healthKitType: .cycling,                       workoutType: .cycle),
        Activity(id: "swim",       label: "Swim",       symbol: "figure.pool.swim",                      healthKitType: .swimming,                      workoutType: .swim),
        Activity(id: "yoga",       label: "Yoga",       symbol: "figure.yoga",                           healthKitType: .yoga,                          workoutType: .yoga),
        Activity(id: "pilates",    label: "Pilates",    symbol: "figure.pilates",                        healthKitType: .pilates,                       workoutType: .pilates),
        Activity(id: "strength",   label: "Strength",   symbol: "figure.strengthtraining.traditional",   healthKitType: .traditionalStrengthTraining,   workoutType: .strength),
        Activity(id: "hiit",       label: "HIIT",       symbol: "figure.highintensity.intervaltraining", healthKitType: .highIntensityIntervalTraining, workoutType: .hiit),
        Activity(id: "cardio",     label: "Cardio",     symbol: "figure.mixed.cardio",                   healthKitType: .mixedCardio,                   workoutType: .cardio),
        Activity(id: "dance",      label: "Dance",      symbol: "figure.dance",                          healthKitType: .cardioDance,                   workoutType: .dance),
        Activity(id: "rowing",     label: "Rowing",     symbol: "figure.rower",                          healthKitType: .rowing,                        workoutType: .rowing),
        Activity(id: "elliptical", label: "Elliptical", symbol: "figure.elliptical",                     healthKitType: .elliptical,                    workoutType: .elliptical),
        Activity(id: "climb",      label: "Climb",      symbol: "figure.climbing",                       healthKitType: .climbing,                      workoutType: .climb),
        Activity(id: "box",        label: "Boxing",     symbol: "figure.boxing",                         healthKitType: .boxing,                        workoutType: .box),
        Activity(id: "stretch",    label: "Stretch",    symbol: "figure.flexibility",                    healthKitType: .flexibility,                   workoutType: .stretch),
        Activity(id: "meditation", label: "Meditation", symbol: "figure.mind.and.body",                  healthKitType: .mindAndBody,                   workoutType: .meditation),
        Activity(id: "ski",        label: "Ski",        symbol: "figure.skiing.downhill",                healthKitType: .downhillSkiing,                workoutType: .ski),
        Activity(id: "snowboard",  label: "Snowboard",  symbol: "figure.snowboarding",                   healthKitType: .snowboarding,                  workoutType: .snowboard),
        Activity(id: "skate",      label: "Skate",      symbol: "figure.skating",                        healthKitType: .skatingSports,                 workoutType: .skate),
        Activity(id: "surf",       label: "Surf",       symbol: "figure.surfing",                        healthKitType: .surfingSports,                 workoutType: .surf),
        Activity(id: "tennis",     label: "Tennis",     symbol: "figure.tennis",                         healthKitType: .tennis,                        workoutType: .tennis),
        Activity(id: "basketball", label: "Basketball", symbol: "figure.basketball",                     healthKitType: .basketball,                    workoutType: .basketball),
        Activity(id: "soccer",     label: "Soccer",     symbol: "figure.soccer",                         healthKitType: .soccer,                        workoutType: .soccer),
        Activity(id: "volleyball", label: "Volleyball", symbol: "figure.volleyball",                     healthKitType: .volleyball,                    workoutType: .volleyball),
        Activity(id: "golf",       label: "Golf",       symbol: "figure.golf",                           healthKitType: .golf,                          workoutType: .golf),
        Activity(id: "rest",       label: "Rest",       symbol: "figure.stand",                          healthKitType: .other,                         workoutType: .rest),
        Activity(id: "other",      label: "Other",      symbol: "figure.mixed.cardio",                   healthKitType: .other,                         workoutType: .other),
    ]

    /// The catalog entry for a shared `WorkoutType` (falls back to "other").
    static func forWorkoutType(_ type: WorkoutType) -> Activity {
        all.first { $0.workoutType == type } ?? all.first { $0.workoutType == .other }!
    }

    /// The catalog entry for an Apple Health activity type, used when importing
    /// workouts read back from HealthKit (falls back to "other").
    static func forHealthKit(_ hk: HKWorkoutActivityType) -> Activity {
        all.first { $0.healthKitType == hk } ?? all.first { $0.workoutType == .other }!
    }
}
