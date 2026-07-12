package com.rajpawardotin.dekhreekh.presentation.vault

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import com.rajpawardotin.dekhreekh.domain.models.WorkoutSession
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme
import org.junit.Rule
import org.junit.Test

class VaultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun vault_whenEmptyState_showsNoTelemetryDataFound() {
        composeTestRule.setContent {
            DekhreekhTheme {
                VaultScreen(uiState = VaultState.Empty)
            }
        }

        composeTestRule.onNodeWithText("No telemetry data found").assertExists()
    }

    @Test
    fun vault_whenHistoryLoaded_showsSessionCards() {
        val dummySessions = listOf(
            WorkoutSession(
                id = "session-1",
                startTime = 1000L,
                activityType = "RUN",
                totalDistanceMeters = 1500f,
                totalDurationSeconds = 600L,
                averagePace = 360L
            ),
            WorkoutSession(
                id = "session-2",
                startTime = 2000L,
                activityType = "RUN",
                totalDistanceMeters = 3000f,
                totalDurationSeconds = 1200L,
                averagePace = 350L
            )
        )

        composeTestRule.setContent {
            DekhreekhTheme {
                VaultScreen(uiState = VaultState.HistoryLoaded(dummySessions))
            }
        }

        // Verify that the UI renders elements for the sessions
        composeTestRule.onNodeWithTag("SessionCard_session-1").assertExists()
        composeTestRule.onNodeWithTag("SessionCard_session-2").assertExists()
        
        // Check for specific UI values formatted from the sessions
        composeTestRule.onNodeWithText("1500.0 m").assertExists()
        composeTestRule.onNodeWithText("3000.0 m").assertExists()
    }
}

// Minimal skeleton for compilation
@androidx.compose.runtime.Composable
fun VaultScreen(uiState: VaultState) {}
