package com.rajpawardotin.dekhreekh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: TelemetryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<TelemetryEntity>)

    @Query("SELECT * FROM raw_telemetry WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getTelemetryForSession(sessionId: String): Flow<List<TelemetryEntity>>

    @Query("SELECT COUNT(*) FROM raw_telemetry")
    fun getPingCount(): Flow<Int>

    @Query("SELECT * FROM raw_telemetry ORDER BY timestamp ASC")
    suspend fun getAllPoints(): List<TelemetryEntity>

    @Query("SELECT * FROM raw_telemetry WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSession(sessionId: String): List<TelemetryEntity>

    @Query("DELETE FROM raw_telemetry")
    suspend fun wipeDatabase()
}
