package com.example.tubesmobdev.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tubesmobdev.service.TokenRefreshAlarmService
import com.example.tubesmobdev.service.TokenRefreshService
import com.example.tubesmobdev.util.ServiceUtil

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm triggered, TokenRefreshService")

//        val workManager = WorkManager.getInstance(context)
//
//        val workRequest = OneTimeWorkRequestBuilder<TokenRefreshWorker>().build()
//        workManager.enqueue(workRequest)
        val serviceIntent = Intent(context, TokenRefreshAlarmService::class.java)
        context.startForegroundService(serviceIntent)

        ServiceUtil.startExactAlarm(context, 3 * 60 * 1000L)
    }
}