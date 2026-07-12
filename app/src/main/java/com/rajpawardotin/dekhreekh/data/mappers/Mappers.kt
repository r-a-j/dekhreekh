package com.rajpawardotin.dekhreekh.data.mappers

import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession

fun SessionEntity.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        activityType = activityType,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds,
        averagePace = averagePace
    )
}

fun TelemetryEntity.toDomain(): TelemetryData {
    return TelemetryData(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        timestamp = timestamp
    )
}

fun WorkoutSession.toEntity(): SessionEntity {
    return SessionEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        activityType = activityType,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds,
        averagePace = averagePace
    )
}

fun TelemetryData.toEntity(sessionId: String): TelemetryEntity {
    return TelemetryEntity(
        sessionId = sessionId,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        timestamp = timestamp
    )
}
