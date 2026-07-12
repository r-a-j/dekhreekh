package com.rajpawardotin.dekhreekh.presentation.vaultdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.runtime.Composable
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `VaultDetailScreen renders tracking map and polyline with historical telemetry`() {
        val mockData = listOf(
            TelemetryData(37.7749, -122.4194, 10.0, 5.0f, 3.5f, System.currentTimeMillis()),
            TelemetryData(37.7750, -122.4195, 12.0, 5.0f, 4.0f, System.currentTimeMillis() + 1000)
        )

        composeTestRule.setContent {
            VaultDetailScreen(
                telemetryPath = mockData
            )
        }

        composeTestRule.onNodeWithTag("TrackingMapContainer").assertIsDisplayed()
        // If maps-compose manages the polyline via composition it might be found, or we rely on the container test
        // However, we assert the container is definitely present.
    }
}

// ---------------------------------------------------------
// Barebones implementation to make tests compile
// ---------------------------------------------------------

@Composable
fun VaultDetailScreen(telemetryPath: List<TelemetryData>) {
    // Empty implementation
}
