package com.example.tubesmobdev.service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList


@UnstableApi
class CustomMediaNotificationProvider(context: Context): DefaultMediaNotificationProvider(context) {
    override fun addNotificationActions(
        mediaSession: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        //[Seek to previous item, Play, Seek to next item, Shuffle, Repeat, Like]
        val defaultPrev = mediaButtons.find { it.displayName == "Seek to previous item" }
        val defaultPlayPause = mediaButtons.indexOfFirst { it.displayName == "Play" || it.displayName == "Pause" }
        val defaultNext = mediaButtons.find { it.displayName == "Seek to next item" }
        val defaultShuffle = mediaButtons.find { it.displayName == "Shuffle" }
        val defaultRepeat = mediaButtons.find { it.displayName == "Repeat" }
        val defaultLike = mediaButtons.indexOfFirst { it.displayName == "Like" }

        Log.d("CustomButton", "${mediaButtons.map { it.displayName }}")

        return intArrayOf(defaultPlayPause, defaultLike)
    }
}