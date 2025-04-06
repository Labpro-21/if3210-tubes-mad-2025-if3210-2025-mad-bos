package com.example.tubesmobdev.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.domain.model.AuthResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TokenRefreshService : Service() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 3 * 60 * 1000L // 1 minutes

    private val tokenCheckRunnable = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                checkTokenValidity()
            }
            handler.postDelayed(this, checkInterval)
        }
    }

    private suspend fun checkTokenValidity() {
        authRepository.verifyToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefresh", "Acesss Token valid")
                    is AuthResult.TokenExpired -> refreshToken()
                    is AuthResult.Failure -> Log.e("TokenRefresh", "Verification failed")
                }
            },
            onFailure = { e -> Log.e("TokenRefresh", "Error", e) }
        )
    }

    private suspend fun refreshToken() {
        authRepository.refreshToken().fold(
            onSuccess = { result ->
                when (result) {
                    is AuthResult.Success -> Log.d("TokenRefresh", "Token refreshed")
                    is AuthResult.TokenExpired -> {
                        authRepository.logout()

                    }
                    is AuthResult.Failure -> Log.e("TokenRefresh", "Refresh failed")
                }
            },
            onFailure = { e -> Log.e("TokenRefresh", "Refresh error", e) }
        )
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenRefresh", "Service started")
        handler.post(tokenCheckRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tokenCheckRunnable)
        Log.d("TokenRefresh", "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}