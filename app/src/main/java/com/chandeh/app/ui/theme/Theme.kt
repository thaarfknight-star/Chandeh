package com.chandeh.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF0B429),
    onPrimary = Color(0xFF231A05),
    secondary = Color(0xFF8B93A7),
    background = Color(0xFF0A0E1A),
    surface = Color(0xFF131B30),
    surfaceVariant = Color(0xFF1B2440),
    onBackground = Color(0xFFF4F6FB),
    onSurface = Color(0xFFF4F6FB),
    error = Color(0xFFF87171),
)

/** سبز/قرمز مخصوص اعداد روی پس‌زمینه‌ی تیره */
val ProfitGreen = Color(0xFF34D399)
val LossRed = Color(0xFFF87171)
val GoldAccent = Color(0xFFF0B429)
val GoldDim = Color(0xFF8A6D2B)

@Composable
fun ChandehTheme(content: @Composable () -> Unit) {
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
