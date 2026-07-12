package com.rajpawardotin.dekhreekh.presentation.vault

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.cancel
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    private lateinit var viewModel: VaultViewModel
    private lateinit var fakeRepository: FakeSessionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSessionRepository()
        viewModel = VaultViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    @Test
    fun `empty database emits VaultState Empty`() = runTest {
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        
        val state = viewModel.uiState.first { it is VaultState.Empty }
        assertTrue(state is VaultState.Empty)
        job.cancel()
    }

    @Test
    fun `repository with data emits VaultState HistoryLoaded`() = runTest {
        val session = WorkoutSession(
            id = "test-session",
            startTime = 1000L,
            activityType = "RUN",
            totalDistanceMeters = 5000f,
            totalDurationSeconds = 1800L,
            averagePace = 360L
        )
        fakeRepository.setSessions(listOf(session))
        
        // Setup viewmodel after populating fake so flows emit the list
        viewModel = VaultViewModel(fakeRepository)
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded }
        assertTrue(state is VaultState.HistoryLoaded)
        val loadedState = state as VaultState.HistoryLoaded
        assertTrue(loadedState.sessions.isNotEmpty())
        assertTrue(loadedState.sessions[0].id == "test-session")
        job.cancel()
    }
}

// Fake Repository for Testing
class FakeSessionRepository : SessionRepository {
    private val sessionsFlow = MutableStateFlow<List<WorkoutSession>>(emptyList())
    
    fun setSessions(sessions: List<WorkoutSession>) {
        sessionsFlow.value = sessions
    }

    override fun getAllSessions(): Flow<List<WorkoutSession>> = sessionsFlow
    
    override suspend fun startSession() = ""
    override suspend fun endSession(id: String, d: Float, t: Long, p: Long) {}
    override suspend fun insertTelemetry(id: String, data: TelemetryData) {}
    override fun getTelemetryForSession(id: String): Flow<List<TelemetryData>> = flowOf()
    override fun getTotalTelemetryCount(): Flow<Int> = flowOf(0)
    override suspend fun getAllTelemetry(): List<TelemetryData> = emptyList()
    override suspend fun wipeAllData() {}
}


