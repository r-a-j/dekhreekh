package com.rajpawardotin.dekhreekh.presentation.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rajpawardotin.dekhreekh.presentation.tracking.TrackingState
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_whenPermissionDenied_showsPermissionPrompt() {
        composeTestRule.setContent {
            DekhreekhTheme {
                DashboardScreen(
                    uiState = TrackingState.Idle,
                    hasLocationPermission = false,
                    onRequestPermission = {},
                    onIntent = {},
                    onNavigateToVault = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Location Permission Required").assertExists()
        composeTestRule.onNodeWithText("Please grant location permission to start tracking your runs.").assertExists()
        composeTestRule.onNodeWithText("Grant Permission").assertExists()
    }

    @Test
    fun dashboard_whenStateIsTracking_showsMetricCards() {
        composeTestRule.setContent {
            DekhreekhTheme {
                DashboardScreen(
                    uiState = TrackingState.Tracking(distance = 1200f, pace = 360L),
                    hasLocationPermission = true,
                    onRequestPermission = {},
                    onIntent = {},
                    onNavigateToVault = {}
                )
            }
        }

        // Verify MetricCard components are rendered
        composeTestRule.onNodeWithTag("MetricCard_Distance").assertExists()
        composeTestRule.onNodeWithTag("MetricCard_Pace").assertExists()
        
        // Verify actual metric values
        composeTestRule.onNodeWithText("1200.0").assertExists() // distance
        composeTestRule.onNodeWithText("360").assertExists() // pace
    }

    @Test
    fun dashboard_whenStateIsIdle_vaultFabIsAccessible() {
        var navigatedToVault = false

        composeTestRule.setContent {
            DekhreekhTheme {
                DashboardScreen(
                    uiState = TrackingState.Idle,
                    hasLocationPermission = true,
                    onRequestPermission = {},
                    onIntent = {},
                    onNavigateToVault = { navigatedToVault = true }
                )
            }
        }

        // Verify Vault FAB is present and clickable
        val vaultFab = composeTestRule.onNodeWithContentDescription("Vault")
        vaultFab.assertExists()
        vaultFab.performClick()

        assertTrue(navigatedToVault)
    }
}
