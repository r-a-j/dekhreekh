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
        val recorder = SessionRecorder(fakeRepository, bufferSizeThreshold = 1)

        val sessionId = recorder.startRecording()

        assertTrue(sessionId.isNotEmpty())
        val session = fakeRepository.sessions.find { it.id == sessionId }
        assertNotNull("Session must be created in the repository", session)
        assertEquals(0f, session?.totalDistanceMeters)
    }

    @Test
    fun `Telemetry Pings correctly map to TelemetryData and insert to repository`() = runTest {
        val fakeRepository = FakeSessionRepository()
        val recorder = SessionRecorder(fakeRepository, bufferSizeThreshold = 1)
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
        val recorder = SessionRecorder(fakeRepository, bufferSizeThreshold = 1)
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

    @Test
    fun `Telemetry Pings are buffered and written in batch`() = runTest {
        val fakeRepository = FakeSessionRepository()
        val recorder = SessionRecorder(fakeRepository, bufferSizeThreshold = 3)
        val sessionId = recorder.startRecording()

        // 1. Process 2 pings (under threshold of 3)
        recorder.processLocationPing(37.7749, -122.4194, 10.0, 5.0f, 2.5f, 100000L)
        recorder.processLocationPing(37.7750, -122.4195, 11.0, 4.0f, 2.6f, 101000L)

        // Verify nothing is in repository yet
        val pingsBeforeThreshold = fakeRepository.telemetry[sessionId]
        assertTrue(pingsBeforeThreshold == null || pingsBeforeThreshold.isEmpty())

        // 2. Process 3rd ping (threshold of 3 hit)
        recorder.processLocationPing(37.7751, -122.4196, 12.0, 3.0f, 2.7f, 102000L)

        // Verify batch of 3 is written
        val pingsAfterThreshold = fakeRepository.telemetry[sessionId]
        assertNotNull(pingsAfterThreshold)
        assertEquals(3, pingsAfterThreshold?.size)

        // 3. Process 4th ping (buffered again)
        recorder.processLocationPing(37.7752, -122.4197, 13.0, 2.0f, 2.8f, 103000L)
        assertEquals(3, fakeRepository.telemetry[sessionId]?.size)

        // 4. Halt engine (forces flush of remaining 1 item)
        recorder.stopRecording(100f, 10L, 5L)
        assertEquals(4, fakeRepository.telemetry[sessionId]?.size)
        assertEquals(37.7752, fakeRepository.telemetry[sessionId]?.last()?.latitude)
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

    override suspend fun insertTelemetryBatch(sessionId: String, dataList: List<TelemetryData>) {
        telemetry.getOrPut(sessionId) { mutableListOf() }.addAll(dataList)
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
