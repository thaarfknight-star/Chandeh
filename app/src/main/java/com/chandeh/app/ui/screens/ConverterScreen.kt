package com.chandeh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chandeh.app.data.Category
import com.chandeh.app.data.PriceItem
import com.chandeh.app.ui.theme.LocalAppTheme
import com.chandeh.app.util.toFaDigits
import com.chandeh.app.util.toFaSmart

private val TomanItem = PriceItem(
    code = "toman_ir",
    titleFa = "تومان ایران",
    category = Category.CURRENCY,
    priceToman = 1L,
    changeToman = 0L,
    changePercent = 0.0,
    updatedAt = ""
)

private val QuickAmounts = listOf("1", "10", "100", "1000")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(items: List<PriceItem>) {
    // «تومان ایران» همیشه در دسترس است، حتی قبل از بارگذاری قیمت‌ها
    val allItems = remember(items) { listOf(TomanItem) + items }
    var amountText by remember { mutableStateOf("1") }
    var fromCode by remember(allItems) {
        mutableStateOf(allItems.find { it.code == "price_dollar_rl" }?.code)
    }
    var toCode by remember(allItems) { mutableStateOf(TomanItem.code) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val from = allItems.find { it.code == fromCode }
    val to = allItems.find { it.code == toCode }
    val result: Double? =
        if (from != null && to != null && to.priceToman > 0)
            amount * from.priceToman.toDouble() / to.priceToman.toDouble()
        else null
    val rate: Double? =
        if (from != null && to != null && to.priceToman > 0)
            from.priceToman.toDouble() / to.priceToman.toDouble()
        else null

    val appTheme = LocalAppTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("تبدیل ارز", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)

        // کارت ورودی با گرادیان تم
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(appTheme.heroTop, appTheme.heroBottom)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "مقدار",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { v -> amountText = v.filter { c -> c.isDigit() || c == '.' } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickAmounts.forEach { q ->
                            AssistChip(
                                onClick = { amountText = q },
                                label = { Text(q.toFaDigits()) }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CurrencyPicker(
                            label = "از",
                            items = allItems,
                            selectedCode = fromCode,
                            onSelect = { fromCode = it },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val tmp = fromCode
                                fromCode = toCode
                                toCode = tmp
                            }
                        ) {
                            Icon(
                                Icons.Filled.SwapVert,
                                contentDescription = "جابه‌جایی",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        CurrencyPicker(
                            label = "به",
                            items = allItems,
                            selectedCode = toCode,
                            onSelect = { toCode = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // کارت نتیجه
        if (result != null && from != null && to != null) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "نتیجه",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "${result.toFaSmart()} ${to.titleFa}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (rate != null) {
                        Text(
                            "هر ۱ ${from.titleFa} = ${rate.toFaSmart()} ${to.titleFa}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        } else if (items.isEmpty()) {
            Text(
                "برای تبدیل، اول قیمت‌ها باید بارگذاری شوند.",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPicker(
    label: String,
    items: List<PriceItem>,
    selectedCode: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = items.find { it.code == selectedCode }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.titleFa ?: "انتخاب کنید",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.titleFa) },
                    onClick = {
                        onSelect(item.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
