package com.chandroid.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chandroid.app.data.PriceItem
import com.chandroid.app.util.toFaDigits
import com.chandroid.app.util.toFaSmart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(items: List<PriceItem>) {
    var amountText by remember { mutableStateOf("1") }
    var fromCode by remember(items) { mutableStateOf(items.firstOrNull()?.code) }
    var toCode by remember(items) { mutableStateOf(items.getOrNull(1)?.code) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val from = items.find { it.code == fromCode }
    val to = items.find { it.code == toCode }
    val result: Double? =
        if (from != null && to != null && to.priceToman > 0)
            amount * from.priceToman.toDouble() / to.priceToman.toDouble()
        else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("تبدیل ارز", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        OutlinedTextField(
            value = amountText,
            onValueChange = { v -> amountText = v.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("مقدار") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        CurrencyPicker(
            label = "از",
            items = items,
            selectedCode = fromCode,
            onSelect = { fromCode = it }
        )
        CurrencyPicker(
            label = "به",
            items = items,
            selectedCode = toCode,
            onSelect = { toCode = it }
        )

        Spacer(Modifier.height(4.dp))

        if (result != null && from != null && to != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${amountText.ifBlank { "۰" }.toFaDigits()} ${from.titleFa} = " +
                            "${result.toFaSmart()} ${to.titleFa}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (items.isEmpty()) {
            Text(
                "برای تبدیل، اول قیمت‌ها باید بارگذاری شوند.",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPicker(
    label: String,
    items: List<PriceItem>,
    selectedCode: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = items.find { it.code == selectedCode }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.titleFa ?: "انتخاب کنید",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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
