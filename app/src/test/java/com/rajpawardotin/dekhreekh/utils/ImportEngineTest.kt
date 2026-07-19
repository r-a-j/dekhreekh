package com.rajpawardotin.dekhreekh.utils

import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.domain.repository.SessionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.Instant

class ImportEngineTest {

    private val sampleGpx = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="Dekhreekh Telemetry Engine">
          <trk>
            <name>Test Run</name>
            <trkseg>
              <trkpt lat="37.7749" lon="-122.4194">
                <ele>10.0</ele>
                <time>2026-05-14T15:30:00Z</time>
              </trkpt>
              <trkpt lat="37.7750" lon="-122.4195">
                <ele>12.5</ele>
                <time>2026-05-14T15:30:10Z</time>
              </trkpt>
            </trkseg>
          </trk>
        </gpx>
    """.trimIndent()

    // ── Fake repository helper ──────────────────────────────────────────────

    private fun fakeRepo() = object : SessionRepository {
        var importedSession: WorkoutSession? = null
        var importedTelemetry: List<TelemetryData> = emptyList()
        var updatedId: String? = null
        var updatedName: String? = null
        var updatedTags: List<String> = emptyList()
        var deletedId: String? = null

        override suspend fun importSession(session: WorkoutSession, telemetry: List<TelemetryData>) {
            importedSession = session
            importedTelemetry = telemetry
        }
        override suspend fun updateSessionMeta(sessionId: String, name: String?, tags: List<String>) {
            updatedId = sessionId; updatedName = name; updatedTags = tags
        }
        override suspend fun deleteSession(sessionId: String) { deletedId = sessionId }
        override suspend fun getTelemetryForSessionOnce(sessionId: String) = emptyList<TelemetryData>()
        override suspend fun startSession() = ""
        override suspend fun endSession(id: String, d: Float, t: Long, p: Long) {}
        override suspend fun insertTelemetry(id: String, data: TelemetryData) {}
        override fun getAllSessions() = flowOf(emptyList<WorkoutSession>())
        override fun getTelemetryForSession(id: String) = flowOf(emptyList<TelemetryData>())
        override fun getTotalTelemetryCount() = flowOf(0)
        override suspend fun getAllTelemetry() = emptyList<TelemetryData>()
        override suspend fun wipeAllData() {}
        override suspend fun renameTagGlobally(oldTag: String, newTag: String) {}
        override suspend fun deleteTagGlobally(tag: String) {}
    }

    // ── Tests ──────────────────────────────────────────────────────────────

    @Test
    fun `importGpxStream correctly parses GPX and inserts workout session with computed statistics`() = runTest {
        val repo = fakeRepo()
        val success = ImportEngine.importGpxStream(ByteArrayInputStream(sampleGpx.toByteArray()), repo)

        assertTrue("Import must be successful", success)
        val session = repo.importedSession!!
        val telemetry = repo.importedTelemetry

        assertEquals(2, telemetry.size)
        assertEquals(37.7749, telemetry[0].latitude, 0.0001)
        assertEquals(-122.4194, telemetry[0].longitude, 0.0001)
        assertEquals(10.0, telemetry[0].altitude, 0.001)
        assertEquals(37.7750, telemetry[1].latitude, 0.0001)
        assertEquals(-122.4195, telemetry[1].longitude, 0.0001)
        assertEquals(12.5, telemetry[1].altitude, 0.001)

        val expectedStart = Instant.parse("2026-05-14T15:30:00Z").toEpochMilli()
        val expectedEnd = Instant.parse("2026-05-14T15:30:10Z").toEpochMilli()
        assertEquals(expectedStart, session.startTime)
        assertEquals(expectedEnd, session.endTime)
        assertEquals(10L, session.totalDurationSeconds)
        assertTrue("Distance should be > 0", session.totalDistanceMeters > 0)
        assertNull("Name should be null when no customName given", session.name)
    }

    @Test
    fun `importGpxStream sets session name when customName is provided`() = runTest {
        val repo = fakeRepo()
        val success = ImportEngine.importGpxStream(
            ByteArrayInputStream(sampleGpx.toByteArray()),
            repo,
            customName = "Morning Trail Run"
        )

        assertTrue(success)
        assertEquals("Morning Trail Run", repo.importedSession?.name)
    }

    @Test
    fun `importGpxStream ignores blank customName and leaves name null`() = runTest {
        val repo = fakeRepo()
        ImportEngine.importGpxStream(
            ByteArrayInputStream(sampleGpx.toByteArray()),
            repo,
            customName = "   "
        )
        assertNull(repo.importedSession?.name)
    }

    @Test
    fun `importGpxStream sets isLowActivity false for tracks with meaningful distance`() = runTest {
        val repo = fakeRepo()
        ImportEngine.importGpxStream(ByteArrayInputStream(sampleGpx.toByteArray()), repo)
        // The two points are ~14m apart — still above the 5m threshold
        assertFalse(
            "isLowActivity should be false for distance > 5m",
            repo.importedSession?.isLowActivity ?: true
        )
    }

    @Test
    fun `importGpxStream sets isLowActivity true and automatically tags with glitch for tracks with low activity`() = runTest {
        val tinyGpx = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="Dekhreekh Telemetry Engine">
              <trk>
                <name>Tiny Walk</name>
                <trkseg>
                  <trkpt lat="37.7749" lon="-122.4194">
                    <ele>10.0</ele>
                    <time>2026-05-14T15:30:00Z</time>
                  </trkpt>
                  <trkpt lat="37.7749" lon="-122.4194">
                    <ele>10.0</ele>
                    <time>2026-05-14T15:30:05Z</time>
                  </trkpt>
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val repo = fakeRepo()
        val success = ImportEngine.importGpxStream(ByteArrayInputStream(tinyGpx.toByteArray()), repo)

        assertTrue(success)
        val session = repo.importedSession!!
        assertTrue("isLowActivity should be true", session.isLowActivity)
        assertTrue("Should contain glitch tag", session.tags.contains("glitch"))
    }
}
