package com.example.tubesmobdev.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tubesmobdev.service.TokenRefreshService

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("RestartReceiver", "Restarting TokenRefreshService")

        val serviceIntent = Intent(context, TokenRefreshService::class.java)

        context.startForegroundService(serviceIntent)
    }
}
