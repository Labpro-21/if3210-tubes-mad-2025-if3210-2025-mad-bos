package com.example.tubesmobdev.manager

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.service.MusicService
import com.example.tubesmobdev.util.RepeatMode
import com.google.gson.Gson
import javax.inject.Inject

class PlayerManager @Inject constructor(
    private val context: Context
) {
    @OptIn(UnstableApi::class)
    fun play(
        song: Song,
        queue: List<Song>,
        isShuffle: Boolean,
        repeatMode: RepeatMode
    ) {
        val index = queue.indexOfFirst { it.id == song.id }.takeIf { it != -1 } ?: 0

        Log.d("PlayerManager", "Playing song: ${song.title}")
        Log.d("PlayerManager", "Queue size: ${queue.size}")
        Log.d("PlayerManager", "Index in queue: $index")

        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY
            putExtra(MusicService.EXTRA_QUEUE, Gson().toJson(queue))
            putExtra(MusicService.EXTRA_INDEX, index)
            putExtra(MusicService.EXTRA_SHUFFLE, isShuffle)
            putExtra(MusicService.EXTRA_REPEAT, repeatMode.name)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    @OptIn(UnstableApi::class)
    fun stop() {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun clearWithCallback(onClear: (() -> Unit)? = null) {
        stop()
        onClear?.invoke()
    }
}