package com.example.tubesmobdev.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tubesmobdev.service.TokenRefreshService
import com.example.tubesmobdev.util.ServiceUtil

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("RestartReceiver", "Restarting TokenRefreshService")

        ServiceUtil.startService(context)
    }
}
