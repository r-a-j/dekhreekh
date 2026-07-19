package com.rajpawardotin.dekhreekh.data.mappers

import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun `toEntity automatically adds glitch tag for distance less than 5 meters`() {
        val session = WorkoutSession(
            id = "test-id",
            startTime = 1000L,
            endTime = 2000L,
            activityType = "RUN",
            totalDistanceMeters = 3.5f, // under 5 meters
            totalDurationSeconds = 10,
            averagePace = 0,
            tags = listOf("jog")
        )

        val entity = session.toEntity()

        assertTrue(entity.isLowActivity)
        val tagsList = entity.tags.split(",")
        assertTrue(tagsList.contains("jog"))
        assertTrue(tagsList.contains("glitch"))
    }

    @Test
    fun `toEntity does not add glitch tag for distance greater than or equal to 5 meters`() {
        val session = WorkoutSession(
            id = "test-id",
            startTime = 1000L,
            endTime = 2000L,
            activityType = "RUN",
            totalDistanceMeters = 15.0f, // over 5 meters
            totalDurationSeconds = 10,
            averagePace = 0,
            tags = listOf("run")
        )

        val entity = session.toEntity()

        assertTrue(!entity.isLowActivity)
        val tagsList = entity.tags.split(",")
        assertTrue(tagsList.contains("run"))
        assertTrue(!tagsList.contains("glitch"))
    }
}
