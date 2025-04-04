package com.example.tubesmobdev.data.local.preferences

import kotlinx.coroutines.flow.Flow

interface IAuthPreferences {
    suspend fun saveAccessToken(token: String)
    suspend fun getToken(): String?
    suspend fun getUserId(): Long?
    suspend fun saveRefreshToken(refreshToken: String)
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
    suspend fun isLoggedIn(): Boolean
    val isLoggedInFlow: Flow<Boolean>
}