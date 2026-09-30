package com.chandeh.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chandeh.app.MainActivity
import com.chandeh.app.data.Prefs
import com.chandeh.app.data.PriceItem
import com.chandeh.app.data.PriceRepository
import com.chandeh.app.ui.theme.AppTheme
import com.chandeh.app.data.ThemeStore
import com.chandeh.app.ui.theme.LossRed
import com.chandeh.app.ui.theme.ProfitGreen
import com.chandeh.app.util.toFaDigits
import com.chandeh.app.util.toFaPercent
import com.chandeh.app.util.toFaToman
import kotlin.math.abs

/** ویجت صفحه‌ی اصلی — هم‌خون با تم فعال برنامه */
class PriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)
        val theme = ThemeStore.themeById(prefs, prefs.getString(Prefs.KEY_THEME, null))
        val brsKey = prefs.getString(Prefs.KEY_BRS_API, null)
        val items = runCatching {
            PriceRepository(brsApiKey = brsKey, prefs = prefs).fetchPrices().getOrNull()
        }.getOrNull()
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
        val fetchedAt = sdf.format(java.util.Date()).toFaDigits()
        val hasStale = items?.any { it.isStale } == true
        provideContent { WidgetContent(theme, items, fetchedAt, hasStale) }
    }
}

class PriceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PriceWidget()
}

/** تازه‌سازی دستی ویجت با لمس دکمه‌ی «تازه‌سازی» */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        PriceWidget().update(context, glanceId)
    }
}

private val WidgetMuted = Color(0xFF8B93A7)

@Composable
private fun WidgetContent(
    theme: AppTheme,
    items: List<PriceItem>?,
    fetchedAt: String,
    hasStale: Boolean
) {
    val dollar = items?.find { it.code == "price_dollar_rl" }
    val gold18 = items?.find { it.code == "geram18" }
    val sekee = items?.find { it.code == "sekee" }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(theme.heroTop)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        // سربرگ: تازه‌سازی + ساعت دریافت | NerkhCheck
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⟳ $fetchedAt" + if (hasStale) " • ذخیره‌شده" else "",
                modifier = GlanceModifier.clickable(actionRunCallback<RefreshWidgetAction>()),
                style = TextStyle(color = ColorProvider(theme.accent2), fontSize = 12.sp)
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "NerkhCheck",
                style = TextStyle(
                    color = ColorProvider(theme.accent),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(GlanceModifier.height(12.dp))

        if (dollar != null) {
            // عنوان دلار (راست‌چین مثل برنامه)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دلار آمریکا • بازار آزاد",
                    style = TextStyle(
                        color = ColorProvider(theme.accent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(GlanceModifier.width(8.dp))
                Box(
                    modifier = GlanceModifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(theme.accent2),
                    content = {}
                )
            }
            Spacer(GlanceModifier.height(8.dp))
            // قیمت بزرگ (راست‌چین مثل برنامه)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "تومان",
                    modifier = GlanceModifier.padding(bottom = 5.dp),
                    style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 14.sp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = dollar.priceToman.toFaToman(),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            // تغییر روزانه + ساعت (راست‌چین مثل برنامه)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dollar.updatedAt.isNotBlank()) {
                    Text(
                        text = "به‌روزرسانی ${dollar.updatedAt.toFaDigits()}",
                        style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 12.sp)
                    )
                    Spacer(GlanceModifier.width(8.dp))
                }
                val chg = dollar.changePercent
                val up = chg >= 0
                Text(
                    text = (if (up) "▲ " else "▼ ") + abs(chg).toFaPercent(),
                    style = TextStyle(
                        color = ColorProvider(if (up) ProfitGreen else LossRed),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(GlanceModifier.height(10.dp))
            // جداکننده
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.12f)),
                content = {}
            )
            Spacer(GlanceModifier.height(10.dp))
            // طلا و سکه
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (sekee != null) {
                    Column {
                        Text(
                            text = "سکه امامی",
                            style = TextStyle(
                                color = ColorProvider(WidgetMuted),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = sekee.priceToman.toFaToman(),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.defaultWeight())
                if (gold18 != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "طلای ۱۸ عیار",
                            style = TextStyle(
                                color = ColorProvider(WidgetMuted),
                                fontSize = 12.sp
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = gold18.priceToman.toFaToman(),
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        } else {
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "در حال دریافت قیمت‌ها...",
                style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 13.sp)
            )
            Spacer(GlanceModifier.defaultWeight())
        }
    }
}
