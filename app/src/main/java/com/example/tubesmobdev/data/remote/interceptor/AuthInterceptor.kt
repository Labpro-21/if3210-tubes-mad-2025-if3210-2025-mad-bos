package com.example.tubesmobdev.data.remote.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.IOException
import java.util.Base64

class AuthInterceptor @Inject constructor(
    private val authPreferences: IAuthPreferences,
    @ApplicationContext private val context: Context
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
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
        Log.d("Token Status","$token")
        if (token != null) {
            val durationLeftSeconds = getTokenDurationLeft(token)
            Log.d("AuthRepository", "Token duration left: $durationLeftSeconds seconds")
        }
        return chain.proceed(modifiedRequest)
    }
    private fun getTokenDurationLeft(token: String): Long {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return 0L
            val payload = parts[1]
            val decodedBytes = Base64.getUrlDecoder().decode(payload)
            val json = String(decodedBytes, Charsets.UTF_8)
            val exp = JSONObject(json).getLong("exp")
            val currentTimeSeconds = System.currentTimeMillis() / 1000
            exp - currentTimeSeconds
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to parse token expiration: ${e.message}")
            0L
        }
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}