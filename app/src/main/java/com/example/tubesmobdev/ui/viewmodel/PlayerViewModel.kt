package com.example.tubesmobdev.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.tubesmobdev.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {
    private var mediaPlayer: MediaPlayer? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0.3f)
    val progress: StateFlow<Float> = _progress

    fun playSong(song: Song) {
        _currentSong.value = song
        mediaPlayer?.release()

        val uri = Uri.parse(song.filePath)
        mediaPlayer = MediaPlayer.create(app, uri)

        mediaPlayer?.apply {
            setOnPreparedListener {
                start()
                _isPlaying.value = true
                startProgressUpdater()
            }

            setOnCompletionListener {
                _isPlaying.value = false
                _progress.value = 1f
            }
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
            }
        }
    }

    fun clearSong() {
        mediaPlayer?.release()
        mediaPlayer = null
        _currentSong.value = null
        _isPlaying.value = false
        _progress.value = 0f
    }

    private fun startProgressUpdater() {
        viewModelScope.launch {
            while (_isPlaying.value && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.let {
                    val progress = it.currentPosition.toFloat() / it.duration
                    _progress.value = progress
                }
                delay(500)
            }
        }
    }
}