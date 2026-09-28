package com.chandroid.app.data

/** دسته‌بندی نمادها */
enum class Category(val titleFa: String) {
    CURRENCY("ارزها"),
    GOLD("طلا"),
    COIN("سکه")
}

/** تعریف یک نماد: کد داخلی TGJU + عنوان فارسی */
data class SymbolDef(
    val code: String,
    val titleFa: String,
    val category: Category
)

/**
 * نمادهای تحت پوشش — ترتیب این لیست همان ترتیبی است که به وب‌سرویس
 * TGJU ارسال می‌شود و سطرهای پاسخ هم به همین ترتیب برمی‌گردند.
 */
val SYMBOLS = listOf(
    // ارزها — بازار آزاد
    SymbolDef("price_dollar_rl", "دلار آمریکا", Category.CURRENCY),
    SymbolDef("price_eur", "یورو", Category.CURRENCY),
    SymbolDef("price_gbp", "پوند انگلیس", Category.CURRENCY),
    SymbolDef("price_aed", "درهم امارات", Category.CURRENCY),
    SymbolDef("price_try", "لیر ترکیه", Category.CURRENCY),
    SymbolDef("price_chf", "فرانک سوئیس", Category.CURRENCY),
    SymbolDef("price_cny", "یوان چین", Category.CURRENCY),
    SymbolDef("price_jpy", "ین ژاپن", Category.CURRENCY),
    SymbolDef("price_cad", "دلار کانادا", Category.CURRENCY),
    SymbolDef("price_aud", "دلار استرالیا", Category.CURRENCY),
    // طلا
    SymbolDef("geram18", "طلای ۱۸ عیار", Category.GOLD),
    SymbolDef("geram24", "طلای ۲۴ عیار", Category.GOLD),
    SymbolDef("mesghal", "مثقال طلا", Category.GOLD),
    // سکه
    SymbolDef("sekee", "سکه امامی", Category.COIN),
    SymbolDef("sekeb", "سکه بهار آزادی", Category.COIN),
    SymbolDef("nim", "نیم‌سکه", Category.COIN),
    SymbolDef("rob", "ربع‌سکه", Category.COIN),
    SymbolDef("gerami", "سکه گرمی", Category.COIN),
)

/** یک قلم قیمت — همه‌ی مبالغ به تومان */
data class PriceItem(
    val code: String,
    val titleFa: String,
    val category: Category,
    val priceToman: Long,
    val changeToman: Long,
    val changePercent: Double,
    val updatedAt: String
)
