package com.rajpawardotin.dekhreekh.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.graphics.Color
import io.github.raj.liquid.LiquidGlassTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4FF00),      // Premium Volt Green
    secondary = Color(0xFF8E8E93),    // Steel Gray
    tertiary = Color(0xFFF3F4F6),     // Ice White
    background = Color(0xFF0A0A0F),   // Pure Void Black
    surface = Color(0xFF181824),      // Charcoal Slate
    onPrimary = Color(0xFF000000),    // Black on Volt
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFF3F4F6)
)

private val LightColorScheme = darkColorScheme( // Force dark theme everywhere for premium dashboard vibe
    primary = Color(0xFFD4FF00),
    secondary = Color(0xFF8E8E93),
    tertiary = Color(0xFFF3F4F6),
    background = Color(0xFF0A0A0F),
    surface = Color(0xFF181824),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFF3F4F6)
)

@Composable
fun DekhreekhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default dynamicColor to false to use custom premium styling
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false // Dark style -> Light icons
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    val customLiquidColors = io.github.raj.liquid.tokens.LiquidColorScheme(
        backgroundTop = Color(0xFF0A0A0F),
        backgroundBottom = Color(0xFF0A0A0F),
        accentPrimary = Color(0xFFD4FF00),
        accentSecondary = Color(0xFF8E8E93),
        glassTint = Color(0xFF040406), // Premium dark obsidian tint for high-contrast legibility of white text
        onBackground = Color.White
    )

    LiquidGlassTheme(colors = customLiquidColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}