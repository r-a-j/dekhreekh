package com.rajpawardotin.dekhreekh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_telemetry")
data class TelemetryPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String, // To group points by workout
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float
)
