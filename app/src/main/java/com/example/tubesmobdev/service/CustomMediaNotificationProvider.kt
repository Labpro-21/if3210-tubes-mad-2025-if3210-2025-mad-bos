package com.example.tubesmobdev.service

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.example.tubesmobdev.R
import com.google.common.collect.ImmutableList


@UnstableApi
class CustomMediaNotificationProvider(context: Context): DefaultMediaNotificationProvider(context) {
    override fun addNotificationActions(
        mediaSession: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        Log.d("CustomButton", "add notif")

        return super.addNotificationActions(mediaSession, mediaButtons, builder, actionFactory)
    }


    override fun getMediaButtons(
        session: MediaSession,
        playerCommands: Player.Commands,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        showPauseButton: Boolean
    ): ImmutableList<CommandButton> {
        val buttons = mutableListOf<CommandButton>()

        val playPauseButton = CommandButton.Builder()
            .setDisplayName("Play/Pause")
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setIconResId(R.drawable.ic_play)
            .setExtras(Bundle().apply {
                putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 0)
            })
            .build()

        val likeButton = CommandButton.Builder()
            .setDisplayName("Like")
            .setSessionCommand(SessionCommand(MusicService.ACTION_TOGGLE_LIKE, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_like)
            .setExtras(Bundle().apply {
                putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, 1)
            })
            .build()

        buttons += playPauseButton
        buttons += likeButton

        Log.d("CustomButton", "media")
        return ImmutableList.copyOf(buttons)
    }
}
