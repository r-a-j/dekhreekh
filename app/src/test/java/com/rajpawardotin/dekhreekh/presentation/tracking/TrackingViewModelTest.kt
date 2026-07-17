package com.rajpawardotin.dekhreekh.presentation.tracking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.service.SessionRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private lateinit var viewModel: TrackingViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        val fakeRepo = object : SessionRepository {
            override suspend fun startSession() = "dummy"
            override suspend fun endSession(sessionId: String, totalDistanceMeters: Float, totalDurationSeconds: Long, averagePace: Long) {}
            override suspend fun insertTelemetry(sessionId: String, data: TelemetryData) {}
            override fun getAllSessions(): Flow<List<WorkoutSession>> = flowOf(emptyList())
            override fun getTelemetryForSession(sessionId: String): Flow<List<TelemetryData>> = flowOf(emptyList())
            override fun getTotalTelemetryCount(): Flow<Int> = flowOf(0)
            override suspend fun getAllTelemetry(): List<TelemetryData> = emptyList()
            override suspend fun wipeAllData() {}
            override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {}
            override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {}
            override suspend fun deleteSession(sessionId: String) {}
            override suspend fun getTelemetryForSessionOnce(sessionId: String) = emptyList<TelemetryData>()
            override suspend fun renameTagGlobally(oldTag: String, newTag: String) {}
            override suspend fun deleteTagGlobally(tag: String) {}
        }
        
        val fakeRecorder = SessionRecorder(fakeRepo)
        
        viewModel = TrackingViewModel(fakeRecorder, fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle or Ready`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val state = viewModel.uiState.value
        assertTrue(state is TrackingState.Idle || state is TrackingState.Ready)
    }

    @Test
    fun `dispatching IgniteEngine intent transitions state to Tracking`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onIntent(TrackingIntent.IgniteEngine)
        
        // Fast-forward coroutines
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TrackingState.Tracking)
    }

    @Test
    fun `dispatching HaltEngine intent transitions state back to Idle`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        // Start tracking first
        viewModel.onIntent(TrackingIntent.IgniteEngine)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Stop tracking
        viewModel.onIntent(TrackingIntent.HaltEngine)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TrackingState.Idle || state is TrackingState.Ready)
    }
}

// Minimal interfaces to ensure test compilation (will be moved to actual implementation later)
sealed interface TrackingState {
    data object Idle : TrackingState
    data object Ready : TrackingState
    data class Tracking(val distance: Float, val pace: Long) : TrackingState
}

sealed interface TrackingIntent {
    data object IgniteEngine : TrackingIntent
    data object HaltEngine : TrackingIntent
}

// Note: To compile, TrackingViewModel needs to be updated to have uiState and onIntent()
