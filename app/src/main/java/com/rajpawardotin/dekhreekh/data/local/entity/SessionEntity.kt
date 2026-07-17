package com.rajpawardotin.dekhreekh.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class SessionEntity(
    @PrimaryKey val id: String, // UUID
    val startTime: Long,
    val endTime: Long? = null,
    val activityType: String = "Running",
    val totalDistanceMeters: Float = 0f,
    val totalDurationSeconds: Long = 0L,
    val averagePace: Long = 0L,
    @ColumnInfo(defaultValue = "") val name: String? = null,
    @ColumnInfo(defaultValue = "") val tags: String = "",
    @ColumnInfo(defaultValue = "0") val isLowActivity: Boolean = false
)
