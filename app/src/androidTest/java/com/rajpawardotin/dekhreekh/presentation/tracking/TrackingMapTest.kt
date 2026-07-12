package com.rajpawardotin.dekhreekh.presentation.tracking

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackingMapTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Idle or Ready state renders the map component without a path`() {
        // Render the map with an empty list of telemetry points
        composeTestRule.setContent {
            TrackingMap(pathPoints = emptyList())
        }

        // Assert the map container itself is displayed
        composeTestRule.onNodeWithTag("TrackingMapContainer")
            .assertIsDisplayed()

        // Assert that the polyline is NOT displayed
        composeTestRule.onNodeWithTag("TrackingPolyline")
            .assertDoesNotExist()
    }

    @Test
    fun `Tracking state with telemetry points renders the map and polyline`() {
        // Create mock telemetry data points
        val mockPoints = listOf(
            TelemetryData(37.7749, -122.4194, 10.0, 5.0f, 2.5f, 100000L),
            TelemetryData(37.7750, -122.4195, 11.0, 4.0f, 3.1f, 100005L),
            TelemetryData(37.7751, -122.4196, 10.5, 4.5f, 2.8f, 100010L)
        )

        // Render the map with the active points
        composeTestRule.setContent {
            TrackingMap(pathPoints = mockPoints)
        }

        // Assert the map container is displayed
        composeTestRule.onNodeWithTag("TrackingMapContainer")
            .assertIsDisplayed()

        // Assert that the polyline is displayed to connect the active points
        composeTestRule.onNodeWithTag("TrackingPolyline")
            .assertIsDisplayed()
    }
}


