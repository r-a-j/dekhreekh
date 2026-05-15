package com.rajpawardotin.dekhreekh.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: TelemetryPoint)

    // Used for the Settings screen to monitor database size
    @Query("SELECT COUNT(*) FROM raw_telemetry")
    fun getPingCount(): Flow<Int>

    // The nuclear option for the Settings screen
    @Query("DELETE FROM raw_telemetry")
    suspend fun wipeDatabase()

    // Pulls the entire run history, sorted by time
    @Query("SELECT * FROM raw_telemetry ORDER BY timestamp ASC")
    suspend fun getAllPoints(): List<TelemetryPoint>
}
