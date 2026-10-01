package com.chandeh.app.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
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

    /**
     * کلاینت مخصوص دانلود فایل‌های حجیم (APK): بدون سقف زمانیِ «کل تماس».
     * کلاینت اصلی callTimeout=۲۵ثانیه دارد؛ دانلود ~۲۰ مگابایت روی اینترنت
     * کند بیشتر از ۲۵ ثانیه طول می‌کشد و تماس نصف‌کاره کشته می‌شد —
     * علت «دانلود ناموفق بود».
     */
    private val downloadClient: OkHttpClient by lazy {
        client.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.MINUTES)
            .build()
    }

    /**
     * دانلود فایل روی دیسک با گزارش پیشرفت؛ در صورت خطا استثنا می‌دهد.
     * onProgress یک لنبدای suspend است تا صداکننده بتواند به نخ اصلی سوییچ کند.
     */
    @Throws(IOException::class)
    suspend fun download(
        url: String,
        dest: File,
        onProgress: suspend (done: Long, total: Long) -> Unit
    ) {
        val req = Request.Builder().url(url).build()
        downloadClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code} for $url")
            val body = resp.body ?: throw IOException("empty body for $url")
            val total = body.contentLength()
            body.byteStream().use { input ->
                dest.outputStream().use { output ->
                    val buf = ByteArray(8192)
                    var done = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        done += n
                        onProgress(done, total)
                    }
                }
            }
        }
    }
}
