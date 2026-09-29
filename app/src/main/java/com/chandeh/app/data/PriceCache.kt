package com.chandeh.app.data

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * کش آخرین قیمت‌های موفق — تا وقتی هیچ منبعی در دسترس نیست
 * (مثلاً ارزها روی اینترنت ملی بدون کلید BRS) اپ خالی نماند.
 * قیمت‌های کش‌شده با isStale=true برمی‌گردند تا در رابط مشخص باشند.
 */
class PriceCache(private val prefs: SharedPreferences) {

    /**
     * فقط قیمت‌های زنده (نه ذخیره‌شده) ذخیره می‌شوند؛ با کش قبلی ادغام
     * می‌شود تا وقتی فقط بخشی از نمادها زنده‌اند، بقیه‌ی کش پاک نشود.
     */
    fun save(items: List<PriceItem>) {
        val live = items.filter { !it.isStale }
        if (live.isEmpty()) return
        val merged = load().toMutableMap()
        for (it in live) merged[it.code] = it
        val arr = JSONArray()
        for (it in merged.values) {
            arr.put(
                JSONObject()
                    .put("code", it.code)
                    .put("titleFa", it.titleFa)
                    .put("category", it.category.name)
                    .put("priceToman", it.priceToman)
                    .put("changeToman", it.changeToman)
                    .put("changePercent", it.changePercent)
                    .put("updatedAt", it.updatedAt)
            )
        }
        prefs.edit()
            .putString(Prefs.KEY_CACHE, arr.toString())
            .putLong(Prefs.KEY_CACHE_AT, System.currentTimeMillis())
            .apply()
    }

    /** بارگذاری کش؛ خالی بودن یعنی کشی نیست یا منقضی شده */
    fun load(): Map<String, PriceItem> {
        // کش قدیمی‌تر از ۷ روز دور ریخته می‌شود
        val age = System.currentTimeMillis() - prefs.getLong(Prefs.KEY_CACHE_AT, 0L)
        if (age > MAX_AGE_MS) return emptyMap()
        val raw = prefs.getString(Prefs.KEY_CACHE, null) ?: return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val defs = SYMBOLS.associateBy { it.code }
            val out = LinkedHashMap<String, PriceItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val code = o.optString("code")
                val def = defs[code] ?: continue
                out[code] = PriceItem(
                    code = code,
                    titleFa = o.optString("titleFa", def.titleFa),
                    category = def.category,
                    priceToman = o.optLong("priceToman"),
                    changeToman = o.optLong("changeToman"),
                    changePercent = o.optDouble("changePercent"),
                    updatedAt = o.optString("updatedAt", ""),
                    isStale = true
                )
            }
            out
        }.getOrDefault(emptyMap())
    }

    companion object {
        private const val MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000
    }
}
