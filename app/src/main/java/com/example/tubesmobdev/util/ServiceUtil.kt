package com.example.tubesmobdev.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.tubesmobdev.receiver.AlarmReceiver
import com.example.tubesmobdev.service.TokenRefreshService

object ServiceUtil {
    fun startExactAlarm(context: Context, intervalMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = System.currentTimeMillis() + intervalMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val fallbackInterval = intervalMillis / 2
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + fallbackInterval,
                pendingIntent
            )
            Log.w("ServiceUtil", "Exact alarm not allowed → Using fallback alarm with shorter interval ($fallbackInterval ms)")
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("ServiceUtil", "Exact Alarm set for $triggerTime")
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
    fun startService(context: Context) {
        val serviceIntent = Intent(context, TokenRefreshService::class.java)

        context.startForegroundService(serviceIntent)
    }

    fun stopService(context: Context) {
        val intent = Intent(context, TokenRefreshService::class.java)
        context.stopService(intent)
    }
}
