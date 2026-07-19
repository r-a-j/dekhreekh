package com.rajpawardotin.dekhreekh.presentation.vaultdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.runtime.Composable
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import io.github.raj.liquid.rememberLiquidState
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
            val liquidState = rememberLiquidState()
            VaultDetailScreen(
                telemetryPath = mockData,
                liquidState = liquidState
            )
        }

        composeTestRule.onNodeWithTag("TrackingMapContainer").assertIsDisplayed()
    }

    @Test
    fun `VaultDetailScreen renders timeline slider when telemetry has multiple points`() {
        val now = System.currentTimeMillis()
        val mockData = listOf(
            TelemetryData(37.7749, -122.4194, 10.0, 5.0f, 3.5f, now),
            TelemetryData(37.7750, -122.4195, 12.0, 5.0f, 4.0f, now + 5000),
            TelemetryData(37.7751, -122.4196, 14.0, 5.0f, 4.5f, now + 10000)
        )

        composeTestRule.setContent {
            val liquidState = rememberLiquidState()
            VaultDetailScreen(
                telemetryPath = mockData,
                liquidState = liquidState
            )
        }

        composeTestRule.onNodeWithTag("TimelineSlider").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DetailMetricsCard").assertIsDisplayed()
    }
}
