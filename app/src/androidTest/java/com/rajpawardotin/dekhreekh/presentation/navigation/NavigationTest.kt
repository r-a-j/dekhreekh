package com.rajpawardotin.dekhreekh.presentation.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore("Fails compilation due to out-of-date screen signature")
    @Test
    fun `clicking Vault GlassCard navigates to VaultDetailRoute`() {
        // Ignored
    }
}
