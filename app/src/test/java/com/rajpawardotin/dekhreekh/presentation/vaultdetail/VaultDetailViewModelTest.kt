package com.rajpawardotin.dekhreekh.presentation.vaultdetail

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when initialized with sessionId, emits telemetry data for that session`() = runTest {
        val testSessionId = "session_123"
        val mockTelemetry = listOf(
            TelemetryData(37.0, -122.0, 10.0, 1f, 5f, 1000L),
            TelemetryData(37.1, -122.1, 12.0, 1f, 6f, 2000L)
        )

        val fakeRepository = object : SessionRepository {
            override fun getTelemetryForSession(sessionId: String) = 
                if (sessionId == testSessionId) flowOf(mockTelemetry) else flowOf(emptyList())
                
            // Provide dummy implementations for the rest
            override fun getAllSessions() = flowOf(emptyList<com.rajpawardotin.dekhreekh.domain.models.WorkoutSession>())
            override suspend fun startSession() = "dummy"
            override suspend fun endSession(sessionId: String, totalDistanceMeters: Float, totalDurationSeconds: Long, averagePace: Long) {}
            override suspend fun insertTelemetry(sessionId: String, data: TelemetryData) {}
            override fun getTotalTelemetryCount() = flowOf(0)
            override suspend fun getAllTelemetry() = emptyList<TelemetryData>()
            override suspend fun wipeAllData() {}
            override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {}
            override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {}
            override suspend fun deleteSession(sessionId: String) {}
            override suspend fun getTelemetryForSessionOnce(sessionId: String) = emptyList<TelemetryData>()
            override suspend fun renameTagGlobally(oldTag: String, newTag: String) {}
            override suspend fun deleteTagGlobally(tag: String) {}
        }

        // We instantiate the ViewModel with a saved state handle or direct param (depending on implementation, here we pass it)
        val viewModel = VaultDetailViewModel(testSessionId, fakeRepository)

        val emissions = mutableListOf<List<TelemetryData>>()
        val job = launch {
            viewModel.telemetryPath.toList(emissions)
        }

        testScheduler.advanceUntilIdle()

        assertEquals(1, emissions.size) // Should emit empty or the initial
        assertEquals(mockTelemetry, emissions.last())
        
        job.cancel()
    }
}


