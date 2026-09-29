package com.chandeh.app.data

/** کلیدهای SharedPreferences مشترک برنامه و ویجت */
object Prefs {
    const val NAME = "nerkhcheck_prefs"
    const val KEY_THEME = "theme_id"
    /** کلید API سایت BRS (اختیاری) — برای قیمت لحظه‌ای ارز روی اینترنت ملی */
    const val KEY_BRS_API = "brs_api_key"
    const val KEY_CACHE = "price_cache_v1"
    const val KEY_CACHE_AT = "price_cache_at"
}
