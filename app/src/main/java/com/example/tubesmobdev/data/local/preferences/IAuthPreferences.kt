package com.example.tubesmobdev.data.local.preferences

interface IAuthPreferences {
    suspend fun saveAccessToken(token: String)
    suspend fun getToken(): String?
    suspend fun saveRefreshToken(refreshToken: String)
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun isLoggedIn(): Boolean
}