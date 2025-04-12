package com.example.tubesmobdev.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.tubesmobdev.data.local.preferences.IServicePreferences
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import com.example.tubesmobdev.manager.PlayerManager
import com.example.tubesmobdev.receiver.RestartReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TokenRefreshService : Service() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var playerManager: PlayerManager

    @Inject
    lateinit var servicePreferences: IServicePreferences


    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 3 * 60 * 1000L // 3 menit

    private val tokenCheckRunnable = object : Runnable {
        override fun run() {
            Log.d("TokenRefreshService", "Running token check...")

            CoroutineScope(Dispatchers.IO).launch {
                if (isInternetAvailable()) {
                    checkTokenValidity()
                } else {
                    Log.d("TokenRefreshService", "No internet → Skip token check")
                }
            }

            handler.postDelayed(this, checkInterval)
        }
    }

    private val restartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.tubesmobdev.ACTION_TRIGGER_RESTART") {
                Log.d("TokenRefreshService", "Trigger restart received → Reset logic")
                handler.removeCallbacks(tokenCheckRunnable)
                handler.post(tokenCheckRunnable)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(restartReceiver, IntentFilter("com.example.tubesmobdev.ACTION_TRIGGER_RESTART"), RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenRefreshService", "Service started")

        startForeground(1, NotificationUtil.createForegroundNotification(this))

        handler.removeCallbacks(tokenCheckRunnable)
        handler.post(tokenCheckRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(restartReceiver)
        handler.removeCallbacks(tokenCheckRunnable)

        CoroutineScope(Dispatchers.IO).launch {
            val shouldRestart = servicePreferences.shouldRestartService.first()
            if (shouldRestart) {
                Log.d("TokenRefreshService", "Service destroyed → Restarting...")
                val intent = Intent(this@TokenRefreshService, RestartReceiver::class.java)
                sendBroadcast(intent)
            } else {
                Log.d("TokenRefreshService", "Service destroyed → No Restart (by config)")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshService", "Token is valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> {
                        Log.e("TokenRefreshService", "Token invalid → Logout")
                        authRepository.logout()
                        playerManager.clearWithCallback()
                    }
                }
            },
            onFailure = { e ->
                Log.e("TokenRefreshService", "Token check error", e)
            }
        )
    }

    private suspend fun refreshToken() {
        Log.d("TokenRefreshService", "Trying to refresh token...")
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefreshService", "Token refreshed successfully")
                    else -> {
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("TokenRefreshService", "App removed from recent apps → Stop Service")

        playerManager.clear()
        CoroutineScope(Dispatchers.IO).launch {
            servicePreferences.setShouldRestartService(false)
        }

        stopSelf()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}