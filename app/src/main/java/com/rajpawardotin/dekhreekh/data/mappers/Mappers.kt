package com.rajpawardotin.dekhreekh.data.mappers

import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession

private const val LOW_ACTIVITY_THRESHOLD_METERS = 5f

fun SessionEntity.toDomain(): WorkoutSession {
    return WorkoutSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        activityType = activityType,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds,
        averagePace = averagePace,
        name = name,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        isLowActivity = isLowActivity
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
    val finalTags = if (totalDistanceMeters < LOW_ACTIVITY_THRESHOLD_METERS) {
        val list = tags.toMutableList()
        if ("glitch" !in list) list.add("glitch")
        if ("bogus" !in list) list.add("bogus")
        list
    } else {
        tags
    }
    return SessionEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        activityType = activityType,
        totalDistanceMeters = totalDistanceMeters,
        totalDurationSeconds = totalDurationSeconds,
        averagePace = averagePace,
        name = name,
        tags = finalTags.joinToString(","),
        isLowActivity = totalDistanceMeters < LOW_ACTIVITY_THRESHOLD_METERS
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
