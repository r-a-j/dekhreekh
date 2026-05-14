package com.rajpawardotin.dekhreekh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TelemetryPoint::class], version = 1, exportSchema = false)
abstract class DekhreekhDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao

    companion object {
        @Volatile
        private var INSTANCE: DekhreekhDatabase? = null

        fun getDatabase(context: Context): DekhreekhDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DekhreekhDatabase::class.java,
                    "dekhreekh_vault.db"
                )
                // Flagship tuning: Keep DB in memory for massive write speeds during workouts
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) 
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
