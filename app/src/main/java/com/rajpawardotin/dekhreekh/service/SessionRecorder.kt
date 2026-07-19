package com.rajpawardotin.dekhreekh.service

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRecorder(
    private val repository: SessionRepository,
    private val bufferSizeThreshold: Int = 5
) {

    private val _activeSessionId = MutableStateFlow("")
    val activeSessionId = _activeSessionId.asStateFlow()

    private val telemetryBuffer = mutableListOf<TelemetryData>()

    suspend fun startRecording(): String {
        telemetryBuffer.clear()
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
            telemetryBuffer.add(data)
            if (telemetryBuffer.size >= bufferSizeThreshold) {
                flushBuffer()
            }
        }
    }

    private suspend fun flushBuffer() {
        if (_activeSessionId.value.isNotEmpty() && telemetryBuffer.isNotEmpty()) {
            val batch = telemetryBuffer.toList()
            telemetryBuffer.clear()
            repository.insertTelemetryBatch(_activeSessionId.value, batch)
        }
    }

    suspend fun stopRecording(
        totalDistanceMeters: Float, 
        totalDurationSeconds: Long, 
        averagePace: Long
    ) {
        if (_activeSessionId.value.isNotEmpty()) {
            flushBuffer()
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
