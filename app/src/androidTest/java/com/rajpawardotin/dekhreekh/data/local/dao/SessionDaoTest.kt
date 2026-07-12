package com.rajpawardotin.dekhreekh.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rajpawardotin.dekhreekh.data.local.DekhreekhDatabase
import com.rajpawardotin.dekhreekh.data.local.entity.SessionEntity
import com.rajpawardotin.dekhreekh.data.local.entity.TelemetryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var db: DekhreekhDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var telemetryDao: TelemetryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, DekhreekhDatabase::class.java
        ).build()
        sessionDao = db.sessionDao()
        telemetryDao = db.telemetryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertSessionAndTelemetry_retrievesRelationalData() = runTest {
        // 1. Create a dummy session
        val session = SessionEntity(
            id = "test-session-123",
            startTime = 100000L,
            activityType = "RUN",
            totalDistanceMeters = 500f,
            totalDurationSeconds = 120L,
            averagePace = 360L
        )

        // 2. Create associated telemetry points
        val telemetry1 = TelemetryEntity(
            sessionId = "test-session-123",
            timestamp = 100001L,
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 10.0,
            accuracy = 5.0f,
            speed = 3.0f
        )
        val telemetry2 = TelemetryEntity(
            sessionId = "test-session-123",
            timestamp = 100002L,
            latitude = 37.7750,
            longitude = -122.4195,
            altitude = 11.0,
            accuracy = 4.0f,
            speed = 3.1f
        )

        // 3. Insert into DAOs
        sessionDao.insertSession(session)
        telemetryDao.insertPoint(telemetry1)
        telemetryDao.insertPoint(telemetry2)

        // 4. Retrieve relational data (This method must be added to SessionDao to compile!)
        val sessionsWithTelemetry = sessionDao.getSessionsWithTelemetry().first()

        // 5. Assertions
        assertTrue(sessionsWithTelemetry.isNotEmpty())
        val relationalData = sessionsWithTelemetry[0]
        assertEquals("test-session-123", relationalData.session.id)
        assertEquals(2, relationalData.telemetry.size)
        assertEquals(100001L, relationalData.telemetry[0].timestamp)
        assertEquals(100002L, relationalData.telemetry[1].timestamp)
    }
}
