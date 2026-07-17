package com.rajpawardotin.dekhreekh.presentation.vault

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // ── Test data ─────────────────────────────────────────────────────────────

    private val now = System.currentTimeMillis()
    private val sessionA = WorkoutSession(
        id = "a", startTime = now - 3000, endTime = now - 1000,
        activityType = "RUN", totalDistanceMeters = 5000f, totalDurationSeconds = 1800,
        averagePace = 360, name = "Morning Run", tags = listOf("morning", "trail")
    )
    private val sessionB = WorkoutSession(
        id = "b", startTime = now - 7200_000, endTime = now - 3600_000,
        activityType = "RUN", totalDistanceMeters = 1000f, totalDurationSeconds = 600,
        averagePace = 600, name = "Short Jog", tags = listOf("evening")
    )
    private val sessionC = WorkoutSession(
        id = "c", startTime = now - 86400_000, endTime = now - 82800_000,
        activityType = "RUN", totalDistanceMeters = 3f, totalDurationSeconds = 30,
        averagePace = 0, name = "Tiny Walk", tags = emptyList(), isLowActivity = true
    )

    // ── Fake repo ─────────────────────────────────────────────────────────────

    private val sessionsFlow = MutableStateFlow<List<WorkoutSession>>(listOf(sessionA, sessionB, sessionC))

    private val fakeRepo = object : SessionRepository {
        var renamedOld: String? = null
        var renamedNew: String? = null
        var deletedTag: String? = null

        override fun getAllSessions() = sessionsFlow
        override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {}
        override suspend fun deleteSession(sessionId: String) {}
        override suspend fun getTelemetryForSessionOnce(sessionId: String) = emptyList<TelemetryData>()
        override suspend fun startSession() = ""
        override suspend fun endSession(id: String, d: Float, t: Long, p: Long) {}
        override suspend fun insertTelemetry(id: String, data: TelemetryData) {}
        override fun getTelemetryForSession(id: String) = kotlinx.coroutines.flow.flowOf(emptyList<TelemetryData>())
        override fun getTotalTelemetryCount() = kotlinx.coroutines.flow.flowOf(0)
        override suspend fun getAllTelemetry() = emptyList<TelemetryData>()
        override suspend fun wipeAllData() {}
        override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {}
        override suspend fun renameTagGlobally(oldTag: String, newTag: String) {
            renamedOld = oldTag
            renamedNew = newTag
        }
        override suspend fun deleteTagGlobally(tag: String) {
            deletedTag = tag
        }
    }

    private lateinit var viewModel: VaultViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = VaultViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Sort tests ────────────────────────────────────────────────────────────

    @Test
    fun `default sort is DATE_DESC — newest session first`() = runTest {
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        // sessionA is newest, sessionC is oldest
        assertEquals("a", sessions.first().id)
        assertEquals("c", sessions.last().id)
    }

    @Test
    fun `sort by DATE_ASC puts oldest session first`() = runTest {
        viewModel.setSortOrder(SortOrder.DATE_ASC)
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals("c", sessions.first().id)
    }

    @Test
    fun `sort by DISTANCE_DESC puts longest session first`() = runTest {
        viewModel.setSortOrder(SortOrder.DISTANCE_DESC)
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals("a", sessions.first().id) // sessionA has 5000m
    }

    @Test
    fun `sort by DURATION_DESC puts longest duration first`() = runTest {
        viewModel.setSortOrder(SortOrder.DURATION_DESC)
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals("a", sessions.first().id) // sessionA has 1800s
    }

    // ── Tag filter tests ──────────────────────────────────────────────────────

    @Test
    fun `tag filter returns only sessions containing that tag`() = runTest {
        viewModel.toggleTagFilter("morning")
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals(1, sessions.size)
        assertEquals("a", sessions.first().id)
    }

    @Test
    fun `multi-tag filter returns sessions with any matching tag`() = runTest {
        viewModel.toggleTagFilter("morning")
        viewModel.toggleTagFilter("evening")
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        // sessionA has 'morning', sessionB has 'evening'
        assertEquals(2, sessions.size)
    }

    @Test
    fun `clearTagFilter restores all sessions`() = runTest {
        viewModel.toggleTagFilter("morning")
        viewModel.clearTagFilter()
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals(3, sessions.size)
    }

    // ── Low-activity toggle tests ─────────────────────────────────────────────

    @Test
    fun `hideLowActivity toggle removes sessions with isLowActivity true`() = runTest {
        viewModel.setHideLowActivity(true)
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertFalse("sessionC with isLowActivity=true must be hidden", sessions.any { it.id == "c" })
        assertEquals(2, sessions.size)
    }

    @Test
    fun `hideLowActivity false keeps all sessions`() = runTest {
        viewModel.setHideLowActivity(false)
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val sessions = state.items.filterIsInstance<VaultListItem.SessionItem>().map { it.session }
        assertEquals(3, sessions.size)
    }

    // ── allTags aggregation ───────────────────────────────────────────────────

    @Test
    fun `allTags contains all unique tags across sessions`() = runTest {
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        assertTrue(state.allTags.containsAll(setOf("morning", "trail", "evening")))
    }

    // ── Grouped headers test ──────────────────────────────────────────────────

    @Test
    fun `date sort includes Header items between session groups`() = runTest {
        // All 3 sessions are on different days, so we expect 3 headers
        val state = viewModel.uiState.first { it is VaultState.HistoryLoaded } as VaultState.HistoryLoaded
        val headers = state.items.filterIsInstance<VaultListItem.Header>()
        assertTrue("Expect at least one date header", headers.isNotEmpty())
    }

    // ── Global Tag Management tests ──────────────────────────────────────────

    @Test
    fun `tagUsageCounts correctly aggregates counts of all tags`() = runTest {
        val counts = viewModel.tagUsageCounts.first()
        // sessionA has tags: "morning", "trail"
        // sessionB has tags: "evening"
        // sessionC has empty tags
        assertEquals(1, counts["morning"])
        assertEquals(1, counts["trail"])
        assertEquals(1, counts["evening"])
        assertNull(counts["nonexistent"])
    }

    @Test
    fun `renameTagGlobally propagates Old and New tag names to repository`() = runTest {
        viewModel.renameTagGlobally("morning", "am-run")
        // Wait a tick for viewModelScope coroutine launch to finish
        testScheduler.advanceUntilIdle()

        val repo = fakeRepo as? Any // To access backing fields
        // Since fakeRepo is instantiated as object, let's verify using the VM's repository directly:
        val repositoryImpl = viewModel.sessionRepository
        // We can access properties directly via reflection or cast:
        val renamedOld = repositoryImpl.javaClass.getDeclaredField("renamedOld").apply { isAccessible = true }.get(repositoryImpl) as? String
        val renamedNew = repositoryImpl.javaClass.getDeclaredField("renamedNew").apply { isAccessible = true }.get(repositoryImpl) as? String
        
        assertEquals("morning", renamedOld)
        assertEquals("am-run", renamedNew)
    }

    @Test
    fun `deleteTagGlobally propagates tag to repository`() = runTest {
        viewModel.deleteTagGlobally("evening")
        testScheduler.advanceUntilIdle()

        val repositoryImpl = viewModel.sessionRepository
        val deletedTag = repositoryImpl.javaClass.getDeclaredField("deletedTag").apply { isAccessible = true }.get(repositoryImpl) as? String
        
        assertEquals("evening", deletedTag)
    }
}
