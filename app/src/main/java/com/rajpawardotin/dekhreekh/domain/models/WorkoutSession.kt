package com.rajpawardotin.dekhreekh.domain.models

data class WorkoutSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val activityType: String,
    val totalDistanceMeters: Float,
    val totalDurationSeconds: Long,
    val averagePace: Long,
    val name: String? = null,
    val tags: List<String> = emptyList(),
    val isLowActivity: Boolean = false
)
