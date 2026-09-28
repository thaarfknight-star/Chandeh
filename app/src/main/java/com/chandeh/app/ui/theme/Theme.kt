package com.chandeh.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/** سبز/قرمز مخصوص اعداد روی پس‌زمینه‌ی تیره */
val ProfitGreen = Color(0xFF34D399)
val LossRed = Color(0xFFF87171)

/** یک تم رنگی اپ: دو لهجه‌ی ترکیبی + گرادیان کارت ویژه */
data class AppTheme(
    val id: String,
    val nameFa: String,
    val accent: Color,
    val accent2: Color,
    val heroTop: Color,
    val heroBottom: Color
)

/** شش تم ترکیب‌رنگی */
val AppThemes = listOf(
    AppTheme(
        id = "aurora",
        nameFa = "شفق قطبی",
        accent = Color(0xFF2DD4BF),
        accent2 = Color(0xFFA78BFA),
        heroTop = Color(0xFF0B2E2A),
        heroBottom = Color(0xFF1E1B3D)
    ),
    AppTheme(
        id = "volcano",
        nameFa = "آتشفشان",
        accent = Color(0xFFFB923C),
        accent2 = Color(0xFFF43F5E),
        heroTop = Color(0xFF3A1E0B),
        heroBottom = Color(0xFF3A0F1E)
    ),
    AppTheme(
        id = "galaxy",
        nameFa = "کهکشان",
        accent = Color(0xFF818CF8),
        accent2 = Color(0xFFF472B6),
        heroTop = Color(0xFF1A1B3D),
        heroBottom = Color(0xFF2E1030)
    ),
    AppTheme(
        id = "beach",
        nameFa = "ساحل",
        accent = Color(0xFF38BDF8),
        accent2 = Color(0xFFFBBF24),
        heroTop = Color(0xFF0B2740),
        heroBottom = Color(0xFF2E2410)
    ),
    AppTheme(
        id = "jungle",
        nameFa = "جنگل",
        accent = Color(0xFF34D399),
        accent2 = Color(0xFFA3E635),
        heroTop = Color(0xFF0B2E1E),
        heroBottom = Color(0xFF232E0B)
    ),
    AppTheme(
        id = "neon",
        nameFa = "نئون",
        accent = Color(0xFF22D3EE),
        accent2 = Color(0xFFE879F9),
        heroTop = Color(0xFF0A2E38),
        heroBottom = Color(0xFF2E1040)
    )
)

fun appThemeById(id: String?): AppTheme =
    AppThemes.find { it.id == id } ?: AppThemes[0]

val LocalAppTheme = compositionLocalOf { AppThemes[0] }

@Composable
fun NerkhCheckTheme(appTheme: AppTheme, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = appTheme.accent,
                onPrimary = Color(0xFF14100A),
                secondary = Color(0xFF8B93A7),
                background = Color(0xFF0A0E1A),
                surface = Color(0xFF131B30),
                surfaceVariant = Color(0xFF1B2440),
                onBackground = Color(0xFFF4F6FB),
                onSurface = Color(0xFFF4F6FB),
                error = Color(0xFFF87171)
            ),
            typography = Typography(
                headlineMedium = TextStyle(fontSize = 22.sp),
                titleLarge = TextStyle(fontSize = 19.sp),
                bodyLarge = TextStyle(fontSize = 16.sp)
            ),
            content = content
        )
    }
}
