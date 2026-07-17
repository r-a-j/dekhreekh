package com.rajpawardotin.dekhreekh.service

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SessionRecorderTest {

    @Test
    fun `Engine Ignite generates session ID and inserts initial WorkoutSession`() = runTest {
        val fakeRepository = FakeSessionRepository()
        val recorder = SessionRecorder(fakeRepository)

        val sessionId = recorder.startRecording()

        assertTrue(sessionId.isNotEmpty())
        val session = fakeRepository.sessions.find { it.id == sessionId }
        assertNotNull("Session must be created in the repository", session)
        assertEquals(0f, session?.totalDistanceMeters)
    }

    @Test
    fun `Telemetry Pings correctly map to TelemetryData and insert to repository`() = runTest {
        val fakeRepository = FakeSessionRepository()
        val recorder = SessionRecorder(fakeRepository)
        val sessionId = recorder.startRecording()

        // Simulate a location ping
        recorder.processLocationPing(
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 10.0,
            accuracy = 5.0f,
            speed = 2.5f,
            timestamp = 100000L
        )

        val pings = fakeRepository.telemetry[sessionId]
        assertNotNull("Telemetry must be recorded for the active session", pings)
        assertEquals(1, pings?.size)
        
        val recordedData = pings?.first()
        assertEquals(37.7749, recordedData?.latitude)
        assertEquals(-122.4194, recordedData?.longitude)
        assertEquals(5.0f, recordedData?.accuracy)
    }

    @Test
    fun `Engine Halt updates the final WorkoutSession metrics`() = runTest {
        val fakeRepository = FakeSessionRepository()
        val recorder = SessionRecorder(fakeRepository)
        val sessionId = recorder.startRecording()

        recorder.stopRecording(
            totalDistanceMeters = 5000f,
            totalDurationSeconds = 1800L,
            averagePace = 360L
        )

        val session = fakeRepository.sessions.find { it.id == sessionId }
        assertNotNull("Session must exist", session)
        assertEquals(5000f, session?.totalDistanceMeters)
        assertEquals(1800L, session?.totalDurationSeconds)
        assertEquals(360L, session?.averagePace)
        assertNotNull("Session must have an end time", session?.endTime)
    }
}

class FakeSessionRepository : SessionRepository {
    val sessions = mutableListOf<WorkoutSession>()
    val telemetry = mutableMapOf<String, MutableList<TelemetryData>>()

    override suspend fun startSession(): String {
        val id = UUID.randomUUID().toString()
        sessions.add(
            WorkoutSession(
                id = id,
                startTime = System.currentTimeMillis(),
                activityType = "RUN",
                totalDistanceMeters = 0f,
                totalDurationSeconds = 0L,
                averagePace = 0L
            )
        )
        return id
    }

    override suspend fun endSession(
        sessionId: String, totalDistanceMeters: Float,
        totalDurationSeconds: Long, averagePace: Long
    ) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index != -1) {
            val old = sessions[index]
            sessions[index] = old.copy(
                endTime = System.currentTimeMillis(),
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                averagePace = averagePace
            )
        }
    }

    override suspend fun insertTelemetry(sessionId: String, data: TelemetryData) {
        telemetry.getOrPut(sessionId) { mutableListOf() }.add(data)
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> = MutableStateFlow(sessions)
    override fun getTelemetryForSession(sessionId: String): Flow<List<TelemetryData>> = flowOf()
    override fun getTotalTelemetryCount(): Flow<Int> = flowOf(0)
    override suspend fun getAllTelemetry(): List<TelemetryData> = emptyList()
    override suspend fun wipeAllData() {}
    override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {
        sessions.add(session)
        this.telemetry[session.id] = telemetry.toMutableList()
    }
    override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {}
    override suspend fun deleteSession(sessionId: String) { sessions.removeAll { it.id == sessionId } }
    override suspend fun getTelemetryForSessionOnce(sessionId: String) = telemetry[sessionId] ?: emptyList()
    override suspend fun renameTagGlobally(oldTag: String, newTag: String) {}
    override suspend fun deleteTagGlobally(tag: String) {}
}
