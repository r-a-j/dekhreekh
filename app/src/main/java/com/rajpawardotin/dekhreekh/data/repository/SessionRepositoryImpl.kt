package com.rajpawardotin.dekhreekh.data.repository

import com.rajpawardotin.dekhreekh.data.local.dao.SessionDao
import com.rajpawardotin.dekhreekh.data.local.dao.TelemetryDao
import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import com.rajpawardotin.dekhreekh.data.mappers.toDomain
import com.rajpawardotin.dekhreekh.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val telemetryDao: TelemetryDao
) : SessionRepository {

    override suspend fun startSession(): String {
        val sessionId = UUID.randomUUID().toString()
        val newSession = SessionEntity(
            id = sessionId,
            startTime = System.currentTimeMillis()
        )
        sessionDao.insertSession(newSession)
        return sessionId
    }

    override suspend fun endSession(
        sessionId: String,
        totalDistanceMeters: Float,
        totalDurationSeconds: Long,
        averagePace: Long
    ) {
        val session = sessionDao.getSessionById(sessionId)
        session?.let {
            val updatedSession = it.copy(
                endTime = System.currentTimeMillis(),
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                averagePace = averagePace
            )
            sessionDao.updateSession(updatedSession)
        }
    }

    override suspend fun insertTelemetry(sessionId: String, data: TelemetryData) {
        telemetryDao.insertPoint(data.toEntity(sessionId))
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTelemetryForSession(sessionId: String): Flow<List<TelemetryData>> {
        return telemetryDao.getTelemetryForSession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalTelemetryCount(): Flow<Int> {
        return telemetryDao.getPingCount()
    }

    override suspend fun getAllTelemetry(): List<TelemetryData> {
        return telemetryDao.getAllPoints().map { it.toDomain() }
    }

    override suspend fun wipeAllData() {
        sessionDao.deleteAllSessions() // This cascades to telemetry
    }
}
