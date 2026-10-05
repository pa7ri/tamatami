package com.mobile.tamatami.domain.training

/**
 * Activities the user can log. The first block are the original phase-aware
 * types the [TrainingRecommender] reasons about; the rest broaden the catalog
 * so richer sessions persist faithfully and can round-trip through Apple Health
 * on iOS (see `ActivityCatalog` in the iOS app, whose `sdkTypeName` values must
 * match these names exactly).
 *
 * Stored by name via `enumAdapter<WorkoutType>()`, so appending cases is
 * backward-compatible with existing rows — no DB migration required.
 */
enum class WorkoutType {
    // Original recommender vocabulary — do not reorder/rename (referenced by name).
    REST,
    YOGA,
    WALK,
    STRENGTH,
    CARDIO,
    HIIT,
    OTHER,

    // Extended catalog.
    RUN,
    HIKE,
    CYCLE,
    SWIM,
    PILATES,
    DANCE,
    ROWING,
    ELLIPTICAL,
    CLIMB,
    BOX,
    STRETCH,
    MEDITATION,
    SKI,
    SNOWBOARD,
    SKATE,
    SURF,
    TENNIS,
    BASKETBALL,
    SOCCER,
    VOLLEYBALL,
    GOLF,
}
