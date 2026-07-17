package com.rajpawardotin.dekhreekh.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rajpawardotin.dekhreekh.data.local.dao.SessionDao
import com.rajpawardotin.dekhreekh.data.local.dao.TelemetryDao
import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity

@Database(
    entities = [SessionEntity::class, TelemetryEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(TagsConverter::class)
abstract class DekhreekhDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun telemetryDao(): TelemetryDao
}
