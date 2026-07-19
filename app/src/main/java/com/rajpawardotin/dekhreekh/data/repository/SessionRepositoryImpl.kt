package com.rajpawardotin.dekhreekh.data.repository

import com.rajpawardotin.dekhreekh.data.local.dao.SessionDao
import com.rajpawardotin.dekhreekh.data.local.dao.TelemetryDao
import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
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
            val tagsList = if (it.tags.isBlank()) emptyList() else it.tags.split(",").map { t -> t.trim() }.filter { t -> t.isNotEmpty() }
            val finalTags = if (totalDistanceMeters < 5f) {
                val list = tagsList.toMutableList()
                if ("glitch" !in list) list.add("glitch")
                list.joinToString(",")
            } else {
                it.tags
            }
            val updatedSession = it.copy(
                endTime = System.currentTimeMillis(),
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                averagePace = averagePace,
                isLowActivity = totalDistanceMeters < 5f,
                tags = finalTags
            )
            sessionDao.updateSession(updatedSession)
        }
    }

    override suspend fun insertTelemetry(sessionId: String, data: TelemetryData) {
        telemetryDao.insertPoint(data.toEntity(sessionId))
    }

    override suspend fun insertTelemetryBatch(sessionId: String, dataList: List<TelemetryData>) {
        if (dataList.isEmpty()) return
        telemetryDao.insertPoints(dataList.map { it.toEntity(sessionId) })
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

    override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {
        sessionDao.insertSession(session.toEntity())
        telemetryDao.insertPoints(telemetry.map { it.toEntity(session.id) })
    }

    override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {
        sessionDao.updateMeta(sessionId, name, tags.joinToString(","))
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteById(sessionId) // Cascade FK removes telemetry automatically
    }

    override suspend fun getTelemetryForSessionOnce(sessionId: String): List<TelemetryData> {
        return telemetryDao.getPointsForSession(sessionId).map { it.toDomain() }
    }

    override suspend fun renameTagGlobally(oldTag: String, newTag: String) {
        val sessions = sessionDao.getAllSessionsOnce()
        sessions.forEach { entity ->
            val tagsList = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",").map { it.trim() }
            if (oldTag in tagsList) {
                val updatedList = tagsList.map { if (it == oldTag) newTag else it }.distinct().filter { it.isNotBlank() }
                val updatedEntity = entity.copy(tags = updatedList.joinToString(","))
                sessionDao.updateSession(updatedEntity)
            }
        }
    }

    override suspend fun deleteTagGlobally(tag: String) {
        val sessions = sessionDao.getAllSessionsOnce()
        sessions.forEach { entity ->
            val tagsList = if (entity.tags.isBlank()) emptyList() else entity.tags.split(",").map { it.trim() }
            if (tag in tagsList) {
                val updatedList = tagsList.filter { it != tag }
                val updatedEntity = entity.copy(tags = updatedList.joinToString(","))
                sessionDao.updateSession(updatedEntity)
            }
        }
    }
}
