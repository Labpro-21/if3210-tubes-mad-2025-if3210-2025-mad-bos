package com.example.tubesmobdev.manager

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.service.CustomMusicService
import com.example.tubesmobdev.util.RepeatMode
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun play(queue: List<Song>, index: Int, shuffle: Boolean, repeatMode: RepeatMode) {
        val queueJson = Gson().toJson(queue)
        val intent = Intent(context, CustomMusicService::class.java).apply {
            action = CustomMusicService.ACTION_PLAY
            putExtra(CustomMusicService.EXTRA_QUEUE, queueJson)
            putExtra(CustomMusicService.EXTRA_INDEX, index)
            putExtra(CustomMusicService.EXTRA_SHUFFLE, shuffle)
            putExtra(CustomMusicService.EXTRA_REPEAT, repeatMode.name)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop() {
        val intent = Intent(context, CustomMusicService::class.java).apply {
            action = CustomMusicService.ACTION_STOP
        }
        context.startService(intent)
    }
}
