package com.chandeh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chandeh.app.data.Category
import com.chandeh.app.data.PriceItem
import com.chandeh.app.ui.theme.LocalAppTheme
import com.chandeh.app.ui.theme.LossRed
import com.chandeh.app.ui.theme.ProfitGreen
import com.chandeh.app.util.toFaDigits
import com.chandeh.app.util.toFaPercent
import com.chandeh.app.util.toFaToman
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(
    items: List<PriceItem>,
    isLoading: Boolean,
    error: String?,
    lastFetchAt: String?,
    onRefresh: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NerkhCheck", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(
                            "قیمت لحظه‌ای ارز، طلا و سکه",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (lastFetchAt != null) {
                            Text(
                                "آخرین دریافت: $lastFetchAt",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "به‌روزرسانی")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && items.isEmpty() ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "در حال دریافت قیمت‌ها...",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                    }

                error != null && items.isEmpty() -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.SignalWifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        error,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onRefresh) { Text("تلاش دوباره") }
                }

                else -> {
                    val grouped = items.groupBy { it.category }
                    val dollar = items.firstOrNull { it.code == "price_dollar_rl" }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (dollar != null) {
                            item(key = "hero") { HeroCard(dollar) }
                        }
                        Category.values().forEach { cat ->
                            val list = grouped[cat].orEmpty()
                                .filter { it.code != "price_dollar_rl" }
                            if (list.isNotEmpty()) {
                                item(key = "header_${cat.name}") {
                                    CategoryHeader(cat.titleFa)
                                }
                                items(list, key = { it.code }) { item ->
                                    PriceRow(item)
                                }
                            }
                        }
                    }
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/** کارت ویژه‌ی دلار با گرادیان طلایی */
@Composable
fun HeroCard(item: PriceItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LocalAppTheme.current.heroTop,
                            LocalAppTheme.current.heroBottom
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LocalAppTheme.current.accent2)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${item.titleFa} • بازار آزاد",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        item.priceToman.toFaToman(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        color = Color.White
                    )
                    Text(
                        "تومان",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChangePill(item, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    if (item.updatedAt.isNotBlank()) {
                        Text(
                            "به‌روزرسانی ${item.updatedAt.toFaDigits()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            LocalAppTheme.current.accent,
                            LocalAppTheme.current.accent2
                        )
                    )
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun PriceRow(item: PriceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.titleFa,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (item.updatedAt.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.updatedAt.toFaDigits(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.priceToman.toFaToman(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                Spacer(Modifier.height(6.dp))
                ChangePill(item, fontSize = 12.sp)
            }
        }
    }
}

/** نشان تغییر قیمت — سبز/قرمز با پس‌زمینه‌ی محو */
@Composable
fun ChangePill(item: PriceItem, fontSize: androidx.compose.ui.unit.TextUnit) {
    val positive = item.changeToman >= 0
    val color = if (positive) ProfitGreen else LossRed
    val arrow = if (positive) "▲" else "▼"
    val bg = if (positive) Color(0xFF34D399).copy(alpha = 0.14f)
    else Color(0xFFF87171).copy(alpha = 0.14f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "$arrow ${abs(item.changePercent).toFaPercent()}",
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
