package com.mobile.tamatami.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mobile.tamatami.data.db.dao.CravingLogDao
import com.mobile.tamatami.data.db.dao.CycleEntryDao
import com.mobile.tamatami.data.db.dao.HormoneLogDao
import com.mobile.tamatami.data.db.dao.MoodLogDao
import com.mobile.tamatami.data.db.dao.PeriodDayDao
import com.mobile.tamatami.data.db.dao.SymptomLogDao
import com.mobile.tamatami.data.db.dao.TamagotchiStateDao
import com.mobile.tamatami.data.db.dao.UserProfileDao
import com.mobile.tamatami.data.db.dao.WaterLogDao
import com.mobile.tamatami.data.db.dao.WorkoutLogDao
import com.mobile.tamatami.data.db.entity.CravingLogEntity
import com.mobile.tamatami.data.db.entity.CycleEntryEntity
import com.mobile.tamatami.data.db.entity.HormoneLogEntity
import com.mobile.tamatami.data.db.entity.MoodLogEntity
import com.mobile.tamatami.data.db.entity.PeriodDayEntity
import com.mobile.tamatami.data.db.entity.SymptomLogEntity
import com.mobile.tamatami.data.db.entity.TamagotchiStateEntity
import com.mobile.tamatami.data.db.entity.UserProfileEntity
import com.mobile.tamatami.data.db.entity.WaterLogEntity
import com.mobile.tamatami.data.db.entity.WorkoutLogEntity

@Database(
    entities = [
        UserProfileEntity::class,
        CycleEntryEntity::class,
        PeriodDayEntity::class,
        MoodLogEntity::class,
        WaterLogEntity::class,
        HormoneLogEntity::class,
        TamagotchiStateEntity::class,
        WorkoutLogEntity::class,
        SymptomLogEntity::class,
        CravingLogEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TamatamiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun cycleEntryDao(): CycleEntryDao
    abstract fun periodDayDao(): PeriodDayDao
    abstract fun moodLogDao(): MoodLogDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun hormoneLogDao(): HormoneLogDao
    abstract fun tamagotchiStateDao(): TamagotchiStateDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun symptomLogDao(): SymptomLogDao
    abstract fun cravingLogDao(): CravingLogDao

    companion object {
        const val NAME = "tamatami.db"
    }
}
