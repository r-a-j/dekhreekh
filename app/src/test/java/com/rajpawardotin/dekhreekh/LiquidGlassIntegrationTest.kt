package com.rajpawardotin.dekhreekh

import org.junit.Assert.assertNotNull
import org.junit.Test

class LiquidGlassIntegrationTest {

    @Test
    fun verifyLiquidGlassLibraryIsLinkedAndClassesLoad() {
        val themeClass = Class.forName("io.github.raj.liquid.LiquidGlassTheme")
        assertNotNull(themeClass)
        
        val tokensClass = Class.forName("io.github.raj.liquid.tokens.LiquidGlassTokens")
        assertNotNull(tokensClass)
    }
}
