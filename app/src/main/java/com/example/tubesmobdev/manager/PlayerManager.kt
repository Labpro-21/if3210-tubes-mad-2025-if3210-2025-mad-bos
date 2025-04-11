package com.example.tubesmobdev.manager

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerManager(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private var onSongCompleted: (() -> Unit)? = null

    var onClear: (() -> Unit)? = null

    fun play(uri: Uri, onComplete: () -> Unit) {
        clear()
        onSongCompleted = onComplete
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(pfd.fileDescriptor)
                    setOnPreparedListener {
                        start()
                        _isPlaying.value = true
                        startProgressUpdater()
                    }
                    setOnCompletionListener {
                        _progress.value = 1f
                        _isPlaying.value = false
                        onSongCompleted?.invoke()
                    }
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerManager", "Error playing song", e)
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
                startProgressUpdater()
            }
        }
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.seekTo((mediaPlayer?.duration!! * progress).toInt())
        _progress.value = progress
    }

    fun clear() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
        _progress.value = 0f
    }

    fun clearWithCallback() {
        clear()
        onClear?.invoke()
    }

    private fun startProgressUpdater() {
        Thread {
            while (mediaPlayer?.isPlaying == true) {
                _progress.value = mediaPlayer!!.currentPosition.toFloat() / mediaPlayer!!.duration
                Thread.sleep(500)
            }
        }.start()
    }
}
