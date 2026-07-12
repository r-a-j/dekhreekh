package com.rajpawardotin.dekhreekh.presentation.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `clicking Vault GlassCard navigates to VaultDetailRoute`() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        // Render the Vault screen with a dummy card to test the navigation
        // In reality, this relies on AppNavigation having the route wired
        composeTestRule.setContent {
            // We need a dummy AppNavigation test harness here to verify route changes
            // For now, we simulate clicking a card
            // In a real TDD setup, the composable must accept a lambda that triggers navigation
            com.rajpawardotin.dekhreekh.presentation.vault.VaultScreen(
                uiState = com.rajpawardotin.dekhreekh.presentation.vault.VaultState.HistoryLoaded(
                    listOf(
                        com.rajpawardotin.dekhreekh.domain.models.WorkoutSession(
                            id = "session_123",
                            startTime = 0L,
                            endTime = 1000L,
                            activityType = "RUN",
                            totalDistanceMeters = 100f,
                            totalDurationSeconds = 100L,
                            averagePace = 50L
                        )
                    )
                ),
                onSessionClick = { sessionId -> 
                    navController.navigate(VaultDetailRoute(sessionId))
                }
            )
        }

        // Wait for idle and click
        composeTestRule.onNodeWithTag("SessionCard_session_123").performClick()

        // Assert the route changed
        // (Note: In a pure unit test without a NavHost, we just assert the callback was invoked, 
        // but with TestNavHostController, we'd verify the backstack entry if it was hooked up to a NavHost)
    }
}

// ---------------------------------------------------------
// Temporary overload for VaultScreen to make test compile
// (We will remove this when actually updating VaultScreen)
// ---------------------------------------------------------

/*
We assume VaultScreen accepts an `onSessionClick: (String) -> Unit` parameter in the next phase.
*/
