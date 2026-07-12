package com.rajpawardotin.dekhreekh.service

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRecorder(private val repository: SessionRepository) {

    private val _activeSessionId = MutableStateFlow("")
    val activeSessionId = _activeSessionId.asStateFlow()

    suspend fun startRecording(): String {
        val newId = repository.startSession()
        _activeSessionId.value = newId
        return newId
    }

    suspend fun processLocationPing(
        latitude: Double, 
        longitude: Double, 
        altitude: Double,
        accuracy: Float, 
        speed: Float, 
        timestamp: Long
    ) {
        if (_activeSessionId.value.isNotEmpty()) {
            val data = TelemetryData(
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                accuracy = accuracy,
                speed = speed,
                timestamp = timestamp
            )
            repository.insertTelemetry(_activeSessionId.value, data)
        }
    }

    suspend fun stopRecording(
        totalDistanceMeters: Float, 
        totalDurationSeconds: Long, 
        averagePace: Long
    ) {
        if (_activeSessionId.value.isNotEmpty()) {
            repository.endSession(
                sessionId = _activeSessionId.value,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                averagePace = averagePace
            )
            _activeSessionId.value = ""
        }
    }
}
