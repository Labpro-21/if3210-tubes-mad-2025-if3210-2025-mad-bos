package com.example.tubesmobdev.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.tubesmobdev.MainActivity
import com.example.tubesmobdev.R

object CustomNotificationHelper {

    const val CHANNEL_ID = "music_channel"
    const val NOTIFICATION_ID = 69

    // Intent action constants
    const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "ACTION_NEXT"
    const val ACTION_PREV = "ACTION_PREV"
    const val ACTION_LIKE = "ACTION_LIKE"
    const val ACTION_SHUFFLE = "ACTION_SHUFFLE"
    const val ACTION_REPEAT = "ACTION_REPEAT"

    fun createNotification(
        context: Context,
        title: String,
        artist: String,
        cover: Bitmap?,
        isPlaying: Boolean
    ): Notification {
        Log.d("CustomNotif", "Custom notification created: $title")


        val remoteViews = RemoteViews(context.packageName, R.layout.notification_custom_player).apply {
            setTextViewText(R.id.titleTextView, title)
            setTextViewText(R.id.artistTextView, artist)
//            setImageViewBitmap(R.id.coverImageView, cover)

            setImageViewResource(
                R.id.playPauseButton,
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            setOnClickPendingIntent(R.id.playPauseButton, pendingIntent(context, ACTION_PLAY_PAUSE))
            setOnClickPendingIntent(R.id.nextButton, pendingIntent(context, ACTION_NEXT))
            setOnClickPendingIntent(R.id.prevButton, pendingIntent(context, ACTION_PREV))
            setOnClickPendingIntent(R.id.likeButton, pendingIntent(context, ACTION_LIKE))
            setOnClickPendingIntent(R.id.shuffleButton, pendingIntent(context, ACTION_SHUFFLE))
            setOnClickPendingIntent(R.id.repeatButton, pendingIntent(context, ACTION_REPEAT))
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(contentIntent)
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .build()
    }

    private fun pendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(action)
        intent.setPackage(context.packageName)
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
