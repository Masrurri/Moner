package com.alviy.moner.core.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds (and caches) one Retrofit per base URL, all sharing the same OkHttpClient
 * (same connection pool, interceptors, timeouts).
 *
 *   val api: RatesApi = factory.create("https://api.exchangerate.host/")
 */
@Singleton
class RetrofitFactory @Inject constructor(
    val client: OkHttpClient,
    private val gson: Gson,
) {
    private val cache = ConcurrentHashMap<String, Retrofit>()

    fun retrofit(baseUrl: String): Retrofit {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return cache.getOrPut(url) {
            Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }

    fun <T> create(baseUrl: String, service: Class<T>): T = retrofit(baseUrl).create(service)

    inline fun <reified T> create(baseUrl: String): T = create(baseUrl, T::class.java)
}