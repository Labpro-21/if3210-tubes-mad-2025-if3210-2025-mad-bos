package com.example.tubesmobdev.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.manager.PlayerManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class TokenRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val playerManager: PlayerManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TokenRefreshWorker", "Running token check...")

        return withContext(Dispatchers.IO) {
            if (isInternetAvailable()) {
                Log.d("TokenRefreshWorker", "Internet available → Verify token")
                checkTokenValidity()
            } else {
                Log.d("TokenRefreshWorker", "No internet → Skip token check")
            }
            Result.success()
        }
    }

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshWorker", "Token is valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshWorker", "Token check failed → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshWorker", "Token check error", e)
            }
        )
    }

    private suspend fun refreshToken() {
        Log.d("TokenRefreshWorker", "Trying to refresh token...")
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshWorker", "Token refreshed successfully")
                    is AuthResult.TokenExpired -> {
                        Log.e("TokenRefreshWorker", "Token expired → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshWorker", "Token refresh failed → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshWorker", "Token refresh error", e)
            }
        )
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
