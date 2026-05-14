package com.rajpawardotin.dekhreekh.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DekhreekhColorScheme = darkColorScheme(
    primary = MatrixGreen,
    secondary = NeonCyan,
    tertiary = AlertRed,
    background = OledBlack,
    surface = OledBlack,
    onPrimary = OledBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun DekhreekhTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DekhreekhColorScheme,
        typography = Typography,
        content = content
    )
}