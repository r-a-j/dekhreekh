package com.rajpawardotin.dekhreekh.domain.models

data class TelemetryData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float,
    val timestamp: Long
)
