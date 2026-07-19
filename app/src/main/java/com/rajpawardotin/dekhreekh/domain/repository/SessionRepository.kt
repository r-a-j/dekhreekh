package com.rajpawardotin.dekhreekh.domain.repository

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun startSession(): String
    suspend fun endSession(sessionId: String, totalDistanceMeters: Float, totalDurationSeconds: Long, averagePace: Long)
    suspend fun insertTelemetry(sessionId: String, data: TelemetryData)
    suspend fun insertTelemetryBatch(sessionId: String, dataList: List<TelemetryData>) {}
    fun getAllSessions(): Flow<List<WorkoutSession>>
    fun getTelemetryForSession(sessionId: String): Flow<List<TelemetryData>>
    fun getTotalTelemetryCount(): Flow<Int>
    suspend fun getAllTelemetry(): List<TelemetryData>
    suspend fun wipeAllData()
    suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>)
    suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>)
    suspend fun deleteSession(sessionId: String)
    suspend fun getTelemetryForSessionOnce(sessionId: String): List<TelemetryData>
    suspend fun renameTagGlobally(oldTag: String, newTag: String)
    suspend fun deleteTagGlobally(tag: String)
}
