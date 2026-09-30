package com.chandeh.app.data

import android.content.SharedPreferences
import com.chandeh.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

/**
 * موتور بررسی آپدیت: روزی یک‌بار آخرین ریلیز گیت‌هاب را چک می‌کند و اگر
 * نسخه‌ی جدیدتری از نسخه‌ی نصب‌شده پیدا شد، اطلاعاتش را برمی‌گرداند تا
 * برنامه داخل خودش آلارم بدهد.
 *
 * همه‌ی خطاها (قطعی اینترنت، بلاک بودن api.github.com و...) بی‌صدا
 * نادیده گرفته می‌شوند؛ این چک هیچ‌وقت نباید کار اصلی برنامه را خراب کند.
 */
object UpdateChecker {
    private const val RELEASES_URL =
        "https://api.github.com/repos/thaarfknight-star/NerkhCheck/releases/latest"
    private const val FALLBACK_URL =
        "https://github.com/thaarfknight-star/NerkhCheck/releases"
    private const val PREF_LAST_CHECK = "update_last_check_day"
    private const val PREF_DISMISSED = "update_dismissed_version"

    data class UpdateInfo(val version: String, val url: String)

    /**
     * اگر نسخه‌ی جدیدی روی گیت‌هاب باشد و امروز هنوز اطلاع‌رسانی نشده
     * باشد، اطلاعاتش را برمی‌گرداند؛ در غیر این صورت null.
     */
    suspend fun check(prefs: SharedPreferences): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // حداکثر روزی یک‌بار
            val today = dayStamp()
            if (prefs.getString(PREF_LAST_CHECK, null) == today) return@withContext null

            val body = HttpClient.get(RELEASES_URL)
            val json = JSONObject(body)
            val remote = json.optString("tag_name", "")
                .trim().removePrefix("v").removePrefix("V")
            if (remote.isEmpty()) return@withContext null

            prefs.edit().putString(PREF_LAST_CHECK, today).apply()

            val local = BuildConfig.VERSION_NAME.trim().removePrefix("v").removePrefix("V")
            val dismissed = prefs.getString(PREF_DISMISSED, null)
            if (isNewer(remote, local) && dismissed != remote) {
                val url = json.optString("html_url", "").ifEmpty { FALLBACK_URL }
                UpdateInfo(remote, url)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** کاربر «بعداً» را زده؛ تا ریلیز بعدی دیگر آلارم نده */
    fun dismiss(prefs: SharedPreferences, version: String) {
        prefs.edit().putString(PREF_DISMISSED, version).apply()
    }

    private fun dayStamp(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** مقایسه‌ی معنایی نسخه‌ها: 1.0.2 بزرگ‌تر از 1.0.1 است */
    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".")
        val l = local.split(".")
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            val b = l.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            if (a != b) return a > b
        }
        return false
    }
}
