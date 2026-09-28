package com.chandroid.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00E5A0),
    onPrimary = Color(0xFF06281D),
    secondary = Color(0xFF7C8DA6),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF16283D),
    surfaceVariant = Color(0xFF1E3249),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    error = Color(0xFFFF6B6B),
)

val ProfitGreen = Color(0xFF16A34A)
val LossRed = Color(0xFFDC2626)

@Composable
fun ChandroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(
            headlineMedium = TextStyle(fontSize = 22.sp),
            titleLarge = TextStyle(fontSize = 19.sp),
            bodyLarge = TextStyle(fontSize = 16.sp),
        ),
        content = content
    )
}
