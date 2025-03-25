package com.example.tubesmobdev.util


import android.content.Context
import android.content.Intent
import com.example.tubesmobdev.service.TokenRefreshService

object ServiceUtil {
    fun start(context: Context) {
        val intent = Intent(context, TokenRefreshService::class.java)
        context.startService(intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, TokenRefreshService::class.java)
        context.stopService(intent)
    }
}