package com.example.tubesmobdev.data.repository

import android.util.Log
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.remote.api.AuthApi
import com.example.tubesmobdev.data.remote.request.LoginRequest
import com.example.tubesmobdev.data.remote.request.RefreshTokenRequest
import com.example.tubesmobdev.data.remote.response.LoginResponse
import com.example.tubesmobdev.data.remote.response.TokenResponse
import com.example.tubesmobdev.domain.model.AuthResult
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authPreferences: IAuthPreferences
): IAuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> {


        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body: LoginResponse? = response.body()
                if (body != null) {
                    authPreferences.saveAccessToken(body.accessToken)
                    authPreferences.saveRefreshToken(body.refreshToken)

                    Result.success(AuthResult.Success)
                } else {
                    Result.success(AuthResult.Failure("Empty response body"))
                }
            } else {
                Log.e("AuthRepository", "Login failed with code: ${response.code()}")

                if (response.code() == 401) {
                    Result.success(AuthResult.Failure("Email atau password salah"))
                }
                else if (response.code() == 400) {
                    Result.success(AuthResult.Failure("Masukan Email atau Password tidak sesuai"))
                }
                else {
                    Result.success(AuthResult.Failure("Login failed: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception: ${e.message}")
            Result.success(AuthResult.Failure("Terjadi kesalahan jaringan"))
        }
    }

    override suspend fun refreshToken(): Result<AuthResult> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))

            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val body: TokenResponse? = response.body()
                if (body != null) {
                    authPreferences.saveAccessToken(body.accessToken)
                    authPreferences.saveRefreshToken(body.refreshToken)

                    Result.success(AuthResult.Success)
                } else {
                    Result.success(AuthResult.Failure("Empty response body"))
                }
            } else {
                Log.e("AuthRepository", "Refresh failed with code: ${response.code()}")
                Result.success(AuthResult.Failure("Token refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Refresh exception: ${e.message}")
            Result.success(AuthResult.Failure("Terjadi kesalahan jaringan"))
        }
    }

    override suspend fun verifyToken(): Result<AuthResult> {
        return try {
            val token = authPreferences.getToken()
                ?: return Result.failure(Exception("No token available"))

            val response = authApi.verifyToken("Bearer $token")
            when {
                response.isSuccessful -> Result.success(AuthResult.Success)
                response.code() == 403 -> {
                    // Token expired → coba refresh
                    refreshToken()
                }
                else -> {
                    Log.e("AuthRepository", "Verify failed: ${response.code()}")
                    Result.success(AuthResult.Failure("Token verification failed: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Verify exception: ${e.message}")
            Result.success(AuthResult.Failure("Terjadi kesalahan jaringan"))

        }
    }

    override suspend fun logout() {
        authPreferences.clearTokens()
    }

    override suspend fun isLoggedIn(): Boolean {
        return authPreferences.isLoggedIn()
    }
}