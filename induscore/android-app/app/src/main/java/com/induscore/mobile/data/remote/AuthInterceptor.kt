package com.induscore.mobile.data.remote

import com.induscore.mobile.config.AppConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference

class AuthInterceptor : Interceptor {
    private val tokenRef = AtomicReference<String?>()

    fun updateToken(token: String?) {
        tokenRef.set(token)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .addHeader("X-Client-Type", AppConfig.CLIENT_TYPE)
        val token = tokenRef.get()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
