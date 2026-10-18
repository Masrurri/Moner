package com.alviy.moner.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adds "Authorization: Bearer <token>" ONLY for hosts in [ApiBaseUrl.GOOGLE_AUTH_HOSTS].
 * Since the OkHttpClient is shared by every base URL, this stops the Gmail token
 * leaking to third-party APIs.
 */
class GoogleAuthInterceptor @Inject constructor(private val tokens: GoogleAuthTokenHolder) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokens.accessToken
        val needsAuth = original.url.host in ApiBaseUrl.GOOGLE_AUTH_HOSTS
        val request = if (needsAuth && !token.isNullOrEmpty())
            original.newBuilder().header("Authorization", "Bearer $token").build()
        else original
        return chain.proceed(request)
    }
}