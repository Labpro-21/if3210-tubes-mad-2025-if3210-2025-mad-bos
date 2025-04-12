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
import com.example.tubesmobdev.receiver.AlarmReceiver
import com.example.tubesmobdev.receiver.RestartReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TokenRefreshAlarmService : Service() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var playerManager: PlayerManager



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenRefreshService", "Service started")
        startForeground(
            1,
            NotificationUtil.createForegroundNotification(this)
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (isInternetAvailable()) {
                checkTokenValidity()
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
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
