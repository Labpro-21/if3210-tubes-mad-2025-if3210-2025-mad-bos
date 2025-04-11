package com.example.tubesmobdev.data.repository
import org.json.JSONObject
import java.util.Base64
import android.content.Context
import android.util.Log
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.local.preferences.IPlayerPreferences
import com.example.tubesmobdev.data.remote.api.AuthApi
import com.example.tubesmobdev.data.remote.request.LoginRequest
import com.example.tubesmobdev.data.remote.request.RefreshTokenRequest
import com.example.tubesmobdev.data.remote.response.LoginResponse
import com.example.tubesmobdev.data.remote.response.TokenResponse
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.util.ServiceUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context,
    private val authPreferences: IAuthPreferences,
    private val playerPreferences: IPlayerPreferences
): IAuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body: LoginResponse? = response.body()
                if (body != null) {
                    authPreferences.saveAccessToken(body.accessToken)
                    authPreferences.saveRefreshToken(body.refreshToken)

                    ServiceUtil.startTokenRefreshService(context)
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
            Log.d("Refresh tokennya", refreshToken)
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
                if (response.code() == 403) {
                    Result.success(AuthResult.TokenExpired);
                } else {
                    Result.success(AuthResult.Failure("Token refresh failed: ${response.code()}"))
                }
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
            Log.d("Ini untuk error code token pada verify", token)
            val response = authApi.verifyToken("Bearer $token")
            when {
                response.isSuccessful -> {
                    // Check token duration left in seconds.
                    val durationLeftSeconds = getTokenDurationLeft(token)
                    Log.d("AuthRepository", "Token duration left: $durationLeftSeconds seconds")
                    if (durationLeftSeconds < 1 * 60) { // Less than 4 minutes
                        Log.e("AuthRepository", "Token considered expired because duration left is less than 1 minutes")
                        Result.success(AuthResult.TokenExpired)
                    } else {
                        Result.success(AuthResult.Success)
                    }
                }
                response.code() == 401 -> {
                    Log.e("AuthRepository", "Verify failed: ${response.code()}")
                    Result.success(AuthResult.TokenExpired)
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


    override suspend fun logout() {
        authPreferences.clearTokens()
        playerPreferences.clearQueue()
        ServiceUtil.stopTokenRefreshService(context)
    }

    override suspend fun isLoggedIn(): Boolean {
        return authPreferences.isLoggedIn()
    }
}