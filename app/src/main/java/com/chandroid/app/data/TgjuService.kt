package com.chandroid.app.data

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * وب‌سرویس عمومی TGJU — بدون نیاز به API Key.
 * پاسخ یک اسنیپت جاوااسکریپت است که جدول متنی قیمت‌ها را داخل innerHTML می‌گذارد؛
 * پارس آن در PriceRepository انجام می‌شود.
 */
interface TgjuService {

    @GET("fa/api/webservice-snippet/")
    suspend fun getPrices(
        @Query("token") token: String = "webservice",
        @Query("items") items: String,
        @Query("opts") opts: String = "diff,time",
        @Query("placeholder") placeholder: String = "tgju-data"
    ): ResponseBody

    companion object {
        // نسخه‌ی http تست‌شده و سالم است؛ در مانیفست usesCleartextTraffic=true
        private const val BASE_URL = "http://platform.tgju.org/"

        fun create(): TgjuService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TgjuService::class.java)
        }
    }
}
