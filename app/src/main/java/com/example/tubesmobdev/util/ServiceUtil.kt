package com.example.tubesmobdev.util


import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.tubesmobdev.service.TokenRefreshService

object ServiceUtil {
    fun start(context: Context) {
        val intent = Intent(context, TokenRefreshService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, TokenRefreshService::class.java)
        context.stopService(intent)
    }
}