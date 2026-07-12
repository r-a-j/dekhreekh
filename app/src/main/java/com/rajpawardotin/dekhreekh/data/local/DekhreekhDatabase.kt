package com.rajpawardotin.dekhreekh.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rajpawardotin.dekhreekh.data.local.dao.SessionDao
import com.rajpawardotin.dekhreekh.data.local.dao.TelemetryDao
import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity

@Database(entities = [SessionEntity::class, TelemetryEntity::class], version = 2, exportSchema = false)
abstract class DekhreekhDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun telemetryDao(): TelemetryDao
}
