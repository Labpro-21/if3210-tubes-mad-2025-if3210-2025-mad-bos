package com.example.tubesmobdev.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.manager.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TokenRefreshService : Service() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var playerManager: PlayerManager

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 3 * 60 * 1000L // 3 menit

    private val tokenCheckRunnable = object : Runnable {
        override fun run() {
            Log.d("TokenRefreshService", "Running token check...")

            CoroutineScope(Dispatchers.IO).launch {
                if (isInternetAvailable()) {
                    Log.d("TokenRefreshService", "Internet available → Verify token")
                    checkTokenValidity()
                } else {
                    Log.d("TokenRefreshService", "No internet → Skip token check")
                }
            }

            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenRefreshService", "Service started")
        handler.post(tokenCheckRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tokenCheckRunnable)
        Log.d("TokenRefreshService", "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshService", "Token is valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshService", "Token check failed → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshService", "Tok" +
                        "en check error", e)
            }
        )
    }

    private suspend fun refreshToken() {
        Log.d("TokenRefreshService", "Trying to refresh token...")
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshService", "Token refreshed successfully")
                    is AuthResult.TokenExpired -> {
                        Log.e("TokenRefreshService", "Token expired → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()

                    }
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshService", "Token refresh failed → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()

                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshService", "Token refresh error", e)
            }
        )
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
