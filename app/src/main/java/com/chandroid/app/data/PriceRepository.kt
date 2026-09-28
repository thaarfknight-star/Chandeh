package com.chandroid.app.data

class PriceRepository(
    private val service: TgjuService = TgjuService.create()
) {
    suspend fun fetchPrices(): Result<List<PriceItem>> = runCatching {
        val itemsParam = SYMBOLS.joinToString(",") { it.code }
        val body = service.getPrices(items = itemsParam).string()
        parse(body)
    }

    /**
     * نمونه سطر پاسخ:
     *   | سکه امامی | 2,465,050,000 | (2.49%) 60,000,000 | ۱۴:۱۰:۴۹ |
     * قیمت‌ها به ریال برمی‌گردند -> تقسیم بر ۱۰ برای تومان.
     * سطرها به همان ترتیب پارامتر items برمی‌گردند، پس با SYMBOLS زیپ می‌کنیم
     * و به نام داخل پاسخ اعتماد نمی‌کنیم.
     */
    fun parse(body: String): List<PriceItem> {
        val rowRegex = Regex(
            """\|\s*([^|]+?)\s*\|\s*([\d,]+)\s*\|\s*\(?\s*(-?[\d.]+)\s*%\s*\)?\s*(-?[\d,]+)\s*\|\s*([^|]*?)\s*\|"""
        )
        val rows = rowRegex.findAll(body).toList()
        if (rows.isEmpty()) throw IllegalStateException("پاسخ سرور قابل خواندن نبود")
        return rows.take(SYMBOLS.size).mapIndexed { index, m ->
            val def = SYMBOLS[index]
            val priceRial = m.groupValues[2].replace(",", "").toLong()
            val pct = m.groupValues[3].toDouble()
            val changeRial = m.groupValues[4].replace(",", "").toLong()
            // علامت درصد را از روی مبلغ تغییر تعیین می‌کنیم (قابل‌اعتمادتر است)
            val signedPct = if (changeRial < 0) -kotlin.math.abs(pct) else kotlin.math.abs(pct)
            PriceItem(
                code = def.code,
                titleFa = def.titleFa,
                category = def.category,
                priceToman = priceRial / 10,
                changeToman = changeRial / 10,
                changePercent = signedPct,
                updatedAt = m.groupValues[5].trim()
            )
        }
    }
}
