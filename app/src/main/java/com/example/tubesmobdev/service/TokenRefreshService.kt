package com.example.tubesmobdev.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.tubesmobdev.data.repository.AuthRepository
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
    private val checkInterval = 4 * 60 * 1000L // 4 minutes

    private val tokenCheckRunnable = object : Runnable {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                authRepository.verifyToken()
                    .onFailure {
                        Log.e("TokenRefreshService", "Token verification failed", it)
                    }
            }
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(tokenCheckRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tokenCheckRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
