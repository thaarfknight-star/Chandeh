package com.chandeh.app.data

import java.io.IOException
import retrofit2.HttpException

class PriceRepository(
    private val service: TgjuService = TgjuService.create()
) {
    suspend fun fetchPrices(): Result<List<PriceItem>> = runCatching {
        // منبع اصلی: صفحه‌ی اصلی TGJU (+ پارامتر ضدکش تا همیشه تازه باشد)
        val homepageError = try {
            val url = TgjuService.HOMEPAGE_URL + "?_=" + System.currentTimeMillis()
            val html = service.fetch(url).string()
            return@runCatching parseHomepage(html)
        } catch (e: Exception) {
            e
        }
        // منبع جایگزین: وب‌سرویس اسنیپت
        try {
            val itemsParam = SYMBOLS.joinToString(",") { it.code }
            val body = service.fetch(TgjuService.SNIPPET_URL + itemsParam).string()
            parseSnippet(body)
        } catch (e2: Exception) {
            throw friendlyError(e2, homepageError)
        }
    }

    /** خطای قابل‌فهم فارسی به‌جای «HTTP 500» */
    private fun friendlyError(e: Throwable, first: Throwable?): Throwable {
        val msg = when {
            e is HttpException && e.code() >= 500 ->
                "سرور قیمت‌ها موقتاً در دسترس نیست؛ چند دقیقه دیگر تلاش کنید"
            first is HttpException && first.code() >= 500 ->
                "سرور قیمت‌ها موقتاً در دسترس نیست؛ چند دقیقه دیگر تلاش کنید"
            e is IOException || first is IOException ->
                "اتصال اینترنت را بررسی کنید و دوباره تلاش کنید"
            else -> "دریافت قیمت‌ها ممکن نشد؛ دوباره تلاش کنید"
        }
        return IllegalStateException(msg, e)
    }

    /**
     * پارس صفحه‌ی اصلی tgju.org — هر سطر:
     * <tr data-market-nameslug="price_dollar_rl" ...>
     *   <td class="nf">2,442,150</td>
     *   <td class="nf"><span class="high">(3.92%) 92,150</span></td>
     *   ...
     *   <td>۱۴:۱۰:۳۱</td>
     * مبالغ به ریال‌اند -> تقسیم بر ۱۰ برای تومان. جهت تغییر از کلاس high/low.
     */
    fun parseHomepage(html: String): List<PriceItem> {
        val items = SYMBOLS.mapNotNull { def ->
            val row = Regex(
                """<tr[^>]*data-market-nameslug="${def.code}".*?</tr>""",
                setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
            ).find(html)?.value ?: return@mapNotNull null

            val priceRial = Regex("""<td[^>]*>([\d,]+)</td>""")
                .find(row)?.groupValues?.get(1)
                ?.replace(",", "")?.toLongOrNull() ?: return@mapNotNull null

            val changeMatch = Regex(
                """<span class="(high|low)">\(([-\d.]+)%\)\s*(-?[\d,]+)</span>"""
            ).find(row)
            val dir = changeMatch?.groupValues?.get(1)
            val pct = changeMatch?.groupValues?.get(2)?.toDoubleOrNull() ?: 0.0
            val changeRial = changeMatch?.groupValues?.get(3)
                ?.replace(",", "")?.toLongOrNull() ?: 0L
            val sign = if (dir == "low") -1 else 1

            val time = Regex("""<td[^>]*>([\d۰-۹]{1,2}:[\d۰-۹]{2}(?::[\d۰-۹]{2})?)</td>""")
                .find(row)?.groupValues?.get(1).orEmpty()

            PriceItem(
                code = def.code,
                titleFa = def.titleFa,
                category = def.category,
                priceToman = priceRial / 10,
                changeToman = sign * (changeRial / 10),
                changePercent = sign * kotlin.math.abs(pct),
                updatedAt = time
            )
        }
        if (items.size < SYMBOLS.size / 2)
            throw IllegalStateException("پاسخ سرور ناقص بود")
        return items
    }

    /**
     * پارس وب‌سرویس اسنیپت (جایگزین) — نمونه سطر:
     *   | سکه امامی | 2,465,050,000 | (2.49%) 60,000,000 | ۱۴:۱۰:۴۹ |
     */
    fun parseSnippet(body: String): List<PriceItem> {
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
