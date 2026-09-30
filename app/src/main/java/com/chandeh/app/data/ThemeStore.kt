package com.chandeh.app.data

import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.chandeh.app.ui.theme.AppTheme
import com.chandeh.app.ui.theme.AppThemes
import org.json.JSONObject

/**
 * مخزن تم‌ها: تم‌های داخلی + تم‌هایی که از «فایل آپدیت» (update.json)
 * دریافت و روی برنامه اعمال شده‌اند — بدون نیاز به نصب مجدد.
 */
object ThemeStore {
    private const val KEY_CONTENT_VERSION = "content_update_version"
    private const val KEY_CONTENT_JSON = "content_update_json"

    /** همه‌ی تم‌ها: داخلی‌ها اول، بعد تم‌های دریافتی از فایل آپدیت */
    fun mergedThemes(prefs: SharedPreferences): List<AppTheme> {
        val remote = parseRemoteThemes(prefs.getString(KEY_CONTENT_JSON, null).orEmpty())
        val remoteIds = remote.map { it.id }.toSet()
        return AppThemes.filter { it.id !in remoteIds } + remote
    }

    fun themeById(prefs: SharedPreferences, id: String?): AppTheme {
        val all = mergedThemes(prefs)
        return all.find { it.id == id } ?: all.first()
    }

    /**
     * فایل آپدیت را اعمال می‌کند؛ فقط اگر نسخه‌اش تازه‌تر باشد.
     * برمی‌گرداند چند تم «واقعاً جدید» اضافه شد.
     */
    fun applyUpdate(prefs: SharedPreferences, jsonStr: String): Int {
        val json = runCatching { JSONObject(jsonStr) }.getOrNull() ?: return 0
        val version = json.optInt("version", 0)
        if (version <= prefs.getInt(KEY_CONTENT_VERSION, 0)) return 0
        val fresh = parseRemoteThemes(jsonStr)
        val oldIds = parseRemoteThemes(prefs.getString(KEY_CONTENT_JSON, null).orEmpty())
            .map { it.id }.toSet()
        val newCount = fresh.count { it.id !in oldIds }
        prefs.edit()
            .putInt(KEY_CONTENT_VERSION, version)
            .putString(KEY_CONTENT_JSON, jsonStr)
            .apply()
        return newCount
    }

    /** پارس تم‌های داخل فایل آپدیت؛ ورودی‌های خراب بی‌صدا رد می‌شوند */
    fun parseRemoteThemes(jsonStr: String): List<AppTheme> {
        if (jsonStr.isBlank()) return emptyList()
        return try {
            val arr = JSONObject(jsonStr).optJSONArray("themes") ?: return emptyList()
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id", "").trim()
                    val nameFa = o.optString("nameFa", "").trim()
                    if (id.isEmpty() || nameFa.isEmpty()) continue
                    val accent = parseColor(o.optString("accent", "")) ?: continue
                    val accent2 = parseColor(o.optString("accent2", "")) ?: continue
                    val heroTop = parseColor(o.optString("heroTop", "")) ?: continue
                    val heroBottom = parseColor(o.optString("heroBottom", "")) ?: continue
                    add(AppTheme(id, nameFa, accent, accent2, heroTop, heroBottom))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseColor(s: String): Color? {
        if (s.isBlank()) return null
        return try {
            Color(android.graphics.Color.parseColor(s.trim()))
        } catch (_: Exception) {
            null
        }
    }
}
