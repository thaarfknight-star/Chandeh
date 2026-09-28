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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chandeh.app.MainActivity
import com.chandeh.app.data.PriceItem
import com.chandeh.app.data.PriceRepository
import com.chandeh.app.util.toFaToman

/** ویجت صفحه‌ی اصلی: دلار، طلای ۱۸ عیار و سکه‌ی امامی */
class PriceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val items = runCatching { PriceRepository().fetchPrices().getOrNull() }.getOrNull()
        provideContent { WidgetContent(items) }
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

private val WidgetBg = Color(0xFF0A0E1A)
private val WidgetAccent = Color(0xFFF0B429)
private val WidgetMuted = Color(0xFF8B93A7)
private val WidgetText = Color(0xFFF4F6FB)

@Composable
private fun WidgetContent(items: List<PriceItem>?) {
    val dollar = items?.find { it.code == "price_dollar_rl" }
    val gold18 = items?.find { it.code == "geram18" }
    val sekee = items?.find { it.code == "sekee" }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBg)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        // ترتیب دستی راست‌چین (Glance از RTL پشتیبانی نمی‌کند)
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تازه‌سازی",
                modifier = GlanceModifier.clickable(actionRunCallback<RefreshWidgetAction>()),
                style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 12.sp)
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "NerkhCheck",
                style = TextStyle(
                    color = ColorProvider(WidgetAccent),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        if (dollar != null) {
            Text(
                text = "دلار آمریکا",
                style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 12.sp)
            )
            Text(
                text = "${dollar.priceToman.toFaToman()} تومان",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sekee != null) {
                    Text(
                        text = "سکه امامی: ${sekee.priceToman.toFaToman()}",
                        style = TextStyle(color = ColorProvider(WidgetText), fontSize = 12.sp)
                    )
                }
                Spacer(GlanceModifier.defaultWeight())
                if (gold18 != null) {
                    Text(
                        text = "طلای ۱۸: ${gold18.priceToman.toFaToman()}",
                        style = TextStyle(color = ColorProvider(WidgetText), fontSize = 12.sp)
                    )
                }
            }
        } else {
            Text(
                text = "در حال دریافت قیمت‌ها...",
                style = TextStyle(color = ColorProvider(WidgetMuted), fontSize = 13.sp)
            )
        }
    }
}
