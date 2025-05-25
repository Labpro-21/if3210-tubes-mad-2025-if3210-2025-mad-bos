package com.example.tubesmobdev.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.model.toMediaItem
import com.example.tubesmobdev.service.MusicService
import com.example.tubesmobdev.util.RepeatMode
import com.google.gson.Gson
import javax.inject.Inject

class PlayerManager @OptIn(UnstableApi::class)
@Inject constructor(
    private val context: Context,
    private val playbackConnection: PlaybackConnection
) {

    @OptIn(UnstableApi::class)
     suspend fun play(
        song: Song,
        queue: List<Song>,
        isShuffle: Boolean,
        repeatMode: RepeatMode,
    ) {
         Log.d("SongPlayed", song.toString())
        val indexInQueue = queue.indexOfFirst { it.id == song.id }
        val finalQueue = if (indexInQueue != -1) queue else listOf(song)
        val finalIndex = if (indexInQueue != -1) indexInQueue else 0

        val mediaItems = finalQueue.map { it.toMediaItem() }
        val controller = playbackConnection.getController()
        val jsonQueue = Gson().toJson(finalQueue)
        val args = Bundle().apply {
            putString("queue", jsonQueue)
        }

        controller.sendCustomCommand(SessionCommand(MusicService.SONG_QUEUE, Bundle.EMPTY), args)
        controller.setMediaItems(mediaItems, finalIndex, 0)
        controller.shuffleModeEnabled = isShuffle
        controller.repeatMode = when (repeatMode) {
            RepeatMode.REPEAT_ONE -> REPEAT_MODE_ONE
            RepeatMode.REPEAT_ALL -> REPEAT_MODE_ALL
            else -> REPEAT_MODE_OFF
        }
        controller.prepare()
        controller.play()
    }

    @OptIn(UnstableApi::class)
    suspend fun stop() {
        val controller = playbackConnection.getController()
        controller.stop()
    }

    suspend fun clearWithCallback(onClear: (() -> Unit)? = null) {
        stop()
        onClear?.invoke()
    }
}