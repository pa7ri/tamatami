package com.mobile.tamatami.data.db

import androidx.room.TypeConverter
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter fun localDateToLong(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter fun longToLocalDate(value: Long?): LocalDate? =
        value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter fun longToInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun moodToString(value: Mood?): String? = value?.name
    @TypeConverter fun stringToMood(value: String?): Mood? = value?.let(Mood::valueOf)

    @TypeConverter fun flowToString(value: PeriodFlow?): String? = value?.name
    @TypeConverter fun stringToFlow(value: String?): PeriodFlow? = value?.let(PeriodFlow::valueOf)

    @TypeConverter fun tamaMoodToString(value: TamagotchiMood?): String? = value?.name
    @TypeConverter fun stringToTamaMood(value: String?): TamagotchiMood? =
        value?.let(TamagotchiMood::valueOf)

    @TypeConverter fun workoutTypeToString(value: WorkoutType?): String? = value?.name
    @TypeConverter fun stringToWorkoutType(value: String?): WorkoutType? =
        value?.let(WorkoutType::valueOf)

    @TypeConverter fun workoutIntensityToString(value: WorkoutIntensity?): String? = value?.name
    @TypeConverter fun stringToWorkoutIntensity(value: String?): WorkoutIntensity? =
        value?.let(WorkoutIntensity::valueOf)

    @TypeConverter fun symptomToString(value: Symptom?): String? = value?.name
    @TypeConverter fun stringToSymptom(value: String?): Symptom? = value?.let(Symptom::valueOf)

    @TypeConverter fun cravingToString(value: CravingHint?): String? = value?.name
    @TypeConverter fun stringToCraving(value: String?): CravingHint? =
        value?.let(CravingHint::valueOf)

    @TypeConverter fun sleepRatingToString(value: SleepRating?): String? = value?.name
    @TypeConverter fun stringToSleepRating(value: String?): SleepRating? =
        value?.let(SleepRating::valueOf)

    @TypeConverter fun timeOfDayToString(value: TimeOfDay?): String? = value?.name
    @TypeConverter fun stringToTimeOfDay(value: String?): TimeOfDay? =
        value?.let(TimeOfDay::valueOf)
}
