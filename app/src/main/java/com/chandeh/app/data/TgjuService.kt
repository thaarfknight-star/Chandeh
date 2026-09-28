package com.chandeh.app.data

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

/**
 * دریافت قیمت‌ها از TGJU.
 *
 * منبع اصلی: صفحه‌ی اصلی www.tgju.org (ساخت‌یافته، داخل <tr data-market-nameslug="...">)
 * منبع جایگزین: وب‌سرویس عمومی platform.tgju.org (گاهی 500 می‌دهد)
 */
interface TgjuService {

    @GET
    suspend fun fetch(@Url url: String): ResponseBody

    companion object {
        const val HOMEPAGE_URL = "https://www.tgju.org/"
        const val SNIPPET_URL =
            "http://platform.tgju.org/fa/api/webservice-snippet/?token=webservice&opts=diff,time&placeholder=tgju-data&items="

        private const val UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"

        fun create(): TgjuService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .header("User-Agent", UA)
                        .header("Accept-Language", "fa-IR,fa;q=0.9")
                        // جلوگیری از کش میانی: همیشه تازه‌ترین صفحه گرفته شود
                        .header("Cache-Control", "no-cache")
                        .header("Pragma", "no-cache")
                        .build()
                    chain.proceed(req)
                }
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://www.tgju.org/")
                .client(client)
                .build()
                .create(TgjuService::class.java)
        }
    }
}
