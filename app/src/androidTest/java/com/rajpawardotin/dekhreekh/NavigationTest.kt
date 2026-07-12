package com.rajpawardotin.dekhreekh

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.rajpawardotin.dekhreekh.presentation.navigation.AppNavigation
import com.rajpawardotin.dekhreekh.presentation.navigation.DashboardRoute
import com.rajpawardotin.dekhreekh.presentation.navigation.VaultRoute
import com.rajpawardotin.dekhreekh.ui.theme.DekhreekhTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.junit.After

class NavigationTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        startKoin {
            // Load mock modules or actual modules for the test
            // modules(appModule)
        }

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            DekhreekhTheme {
                AppNavigation(navController = navController)
            }
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun appNavigation_verifyStartDestination() {
        // Assert that the starting destination is DashboardRoute
        val route = navController.currentBackStackEntry?.destination?.route
        // Compose Navigation generates the route name from the fully qualified class name for type-safe routes
        assertTrue(route?.contains("DashboardRoute") == true)
    }

    @Test
    fun appNavigation_navigateToVault_viaFab() {
        // Find the Vault FAB by content description and click it
        composeTestRule.onNodeWithContentDescription("Vault").performClick()

        // Verify that the route has changed to VaultRoute
        val route = navController.currentBackStackEntry?.destination?.route
        assertTrue(route?.contains("VaultRoute") == true)
        
        // Verify Vault content is displayed
        composeTestRule.onNodeWithText("Vault").assertExists()
    }
}
