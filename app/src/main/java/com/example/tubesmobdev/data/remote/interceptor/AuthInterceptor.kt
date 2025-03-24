package com.example.tubesmobdev.data.remote.interceptor

import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authPreferences: IAuthPreferences
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.contains("login") ||
            request.url.encodedPath.contains("refresh-token")) {
            return chain.proceed(request)
        }

        val token = runBlocking {
            authPreferences.getToken()
        }
        val modifiedRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else request

        return chain.proceed(modifiedRequest)
    }
}