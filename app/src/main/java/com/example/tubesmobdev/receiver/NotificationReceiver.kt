package com.example.tubesmobdev.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.tubesmobdev.notification.CustomNotificationHelper
import com.example.tubesmobdev.service.MusicService

class NotificationReceiver : BroadcastReceiver() {
    @OptIn(UnstableApi::class)
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            CustomNotificationHelper.ACTION_PLAY_PAUSE,
            CustomNotificationHelper.ACTION_NEXT,
            CustomNotificationHelper.ACTION_PREV,
            CustomNotificationHelper.ACTION_LIKE,
            CustomNotificationHelper.ACTION_SHUFFLE,
            CustomNotificationHelper.ACTION_REPEAT -> {
                Log.d("Receiver", "Action received: ${intent.action}")
                val serviceIntent = Intent(context, MusicService::class.java).apply {
                    action = intent.action
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
