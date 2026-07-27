package com.mobile.tamatami.data.db

import com.mobile.tamatami.db.CravingLog
import com.mobile.tamatami.db.CycleEntry
import com.mobile.tamatami.db.HormoneLog
import com.mobile.tamatami.db.Medication
import com.mobile.tamatami.db.MedicationIntake
import com.mobile.tamatami.db.MoodLog
import com.mobile.tamatami.db.PeriodDay
import com.mobile.tamatami.db.SleepLog
import com.mobile.tamatami.db.SymptomLog
import com.mobile.tamatami.db.TamagotchiState
import com.mobile.tamatami.db.TamatamiDb
import com.mobile.tamatami.db.UserProfile
import com.mobile.tamatami.db.WaterLog
import com.mobile.tamatami.db.WorkoutLog
import com.mobile.tamatami.domain.medication.TimeOfDay
import com.mobile.tamatami.domain.model.Mood
import com.mobile.tamatami.domain.model.PeriodFlow
import com.mobile.tamatami.domain.model.Symptom
import com.mobile.tamatami.domain.model.TamagotchiMood
import com.mobile.tamatami.domain.sleep.SleepRating
import com.mobile.tamatami.domain.nutrition.CravingHint
import com.mobile.tamatami.domain.training.WorkoutIntensity
import com.mobile.tamatami.domain.training.WorkoutType

/**
 * Assembles [TamatamiDb] with every table's column adapters. The [DriverFactory]
 * is platform-supplied (expect/actual); the adapters are shared. This is the one
 * place the generated schema is instantiated.
 */
object DatabaseFactory {
    fun create(driverFactory: DriverFactory): TamatamiDb {
        val driver = driverFactory.createDriver()
        return TamatamiDb(
            driver = driver,
            cravingLogAdapter = CravingLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                cravingAdapter = enumAdapter<CravingHint>(),
            ),
            cycleEntryAdapter = CycleEntry.Adapter(
                idAdapter = longAdapter,
                startDateAdapter = localDateAdapter,
                endDateAdapter = localDateAdapter,
                lengthDaysAdapter = intAdapter,
            ),
            hormoneLogAdapter = HormoneLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                value_Adapter = floatAdapter,
            ),
            medicationAdapter = Medication.Adapter(
                idAdapter = longAdapter,
                dosesPerDayAdapter = intAdapter,
                slotsMaskAdapter = intAdapter,
                createdAtAdapter = instantAdapter,
            ),
            medicationIntakeAdapter = MedicationIntake.Adapter(
                idAdapter = longAdapter,
                medicationIdAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                slotAdapter = enumAdapter<TimeOfDay>(),
            ),
            moodLogAdapter = MoodLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                moodAdapter = enumAdapter<Mood>(),
                energyAdapter = intAdapter,
            ),
            periodDayAdapter = PeriodDay.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                flowAdapter = enumAdapter<PeriodFlow>(),
            ),
            sleepLogAdapter = SleepLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                bedMinuteOfDayAdapter = intAdapter,
                wakeMinuteOfDayAdapter = intAdapter,
                durationMinutesAdapter = intAdapter,
                ratingAdapter = enumAdapter<SleepRating>(),
            ),
            symptomLogAdapter = SymptomLog.Adapter(
                dateAdapter = localDateAdapter,
                symptomAdapter = enumAdapter<Symptom>(),
            ),
            tamagotchiStateAdapter = TamagotchiState.Adapter(
                idAdapter = intAdapter,
                lastMoodAdapter = enumAdapter<TamagotchiMood>(),
            ),
            userProfileAdapter = UserProfile.Adapter(
                idAdapter = intAdapter,
                lastPeriodStartAdapter = localDateAdapter,
                avgCycleLengthDaysAdapter = intAdapter,
                avgPeriodLengthDaysAdapter = intAdapter,
                createdAtAdapter = instantAdapter,
                dailyStepsGoalAdapter = intAdapter,
                sleepGoalMinutesAdapter = intAdapter,
                waterGoalGlassesAdapter = intAdapter,
                waterReminderIntervalHoursAdapter = intAdapter,
            ),
            waterLogAdapter = WaterLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                glassesAdapter = intAdapter,
                goalAdapter = intAdapter,
            ),
            workoutLogAdapter = WorkoutLog.Adapter(
                idAdapter = longAdapter,
                dateAdapter = localDateAdapter,
                typeAdapter = enumAdapter<WorkoutType>(),
                durationMinutesAdapter = intAdapter,
                intensityAdapter = enumAdapter<WorkoutIntensity>(),
            ),
        )
    }
}
