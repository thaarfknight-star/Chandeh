package com.chandeh.app.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * کلاینت HTTP مشترک همه‌ی منابع قیمت.
 *
 * تایم‌اوت‌ها کوتاه انتخاب شده‌اند تا روی اینترنت ملی (که سرورهای خارجی
 * در دسترس نیستند) سریع فیل شود و نوبت به منبع داخلی برسد.
 */
object HttpClient {
    private const val UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"

    val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", UA)
                    .header("Accept-Language", "fa-IR,fa;q=0.9")
                    // جلوگیری از کش میانی: همیشه تازه‌ترین پاسخ گرفته شود
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .build()
                chain.proceed(req)
            }
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    /** GET ساده؛ در صورت خطای HTTP یا شبکه استثنا می‌دهد */
    @Throws(IOException::class)
    fun get(url: String): String {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code} for $url")
            return body
        }
    }

    /** POST با بدنه‌ی JSON؛ در صورت خطای HTTP یا شبکه استثنا می‌دهد */
    @Throws(IOException::class)
    fun post(url: String, jsonBody: String): String {
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        client.newCall(req).execute().use { resp ->
            val respBody = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code} for $url")
            return respBody
        }
    }
}
