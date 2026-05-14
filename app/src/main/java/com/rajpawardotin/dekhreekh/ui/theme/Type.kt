package com.rajpawardotin.dekhreekh.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rajpawardotin.dekhreekh.R

val InterFamily = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        letterSpacing = (-1.5).sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        color = TextSecondary
    )
)