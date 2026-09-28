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

/** یک تم رنگی اپ: لهجه + گرادیان کارت ویژه */
data class AppTheme(
    val id: String,
    val nameFa: String,
    val accent: Color,
    val heroTop: Color,
    val heroBottom: Color
)

/** شش تم رنگی */
val AppThemes = listOf(
    AppTheme(
        id = "gold",
        nameFa = "طلایی",
        accent = Color(0xFFF0B429),
        heroTop = Color(0xFF2B2111),
        heroBottom = Color(0xFF171A2E)
    ),
    AppTheme(
        id = "emerald",
        nameFa = "زمردی",
        accent = Color(0xFF34D399),
        heroTop = Color(0xFF0B2E22),
        heroBottom = Color(0xFF101A2E)
    ),
    AppTheme(
        id = "ocean",
        nameFa = "اقیانوسی",
        accent = Color(0xFF38BDF8),
        heroTop = Color(0xFF0B2740),
        heroBottom = Color(0xFF101A2E)
    ),
    AppTheme(
        id = "violet",
        nameFa = "بنفش",
        accent = Color(0xFFA78BFA),
        heroTop = Color(0xFF241545),
        heroBottom = Color(0xFF141A2E)
    ),
    AppTheme(
        id = "sunset",
        nameFa = "غروب",
        accent = Color(0xFFFB923C),
        heroTop = Color(0xFF3A1E0B),
        heroBottom = Color(0xFF1A142E)
    ),
    AppTheme(
        id = "rose",
        nameFa = "رز",
        accent = Color(0xFFF472B6),
        heroTop = Color(0xFF3A1025),
        heroBottom = Color(0xFF1A142E)
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
