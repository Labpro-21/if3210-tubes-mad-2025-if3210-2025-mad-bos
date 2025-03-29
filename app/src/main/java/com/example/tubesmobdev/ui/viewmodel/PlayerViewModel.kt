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
import com.example.tubesmobdev.data.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val app: Application,
    private val songRepository: SongRepository
) : AndroidViewModel(app) {
    private var mediaPlayer: MediaPlayer? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0.3f)
    val progress: StateFlow<Float> = _progress

    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    val songList: StateFlow<List<Song>> = _songList

    init {
        fetchSongs()
    }

    private fun fetchSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs().collect { songs ->
                _songList.value = songs
            }
        }
    }

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

                viewModelScope.launch {
                    val currentTimestamp = System.currentTimeMillis()
                    songRepository.updateLastPlayed(song.id, currentTimestamp)
                }
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

    fun toggleLike() {
        currentSong.value?.let { song ->
            val newLikedState = !song.isLiked
            _currentSong.value = song.copy(isLiked = newLikedState)
            viewModelScope.launch {
                songRepository.updateLikedStatus(song.id, newLikedState)
            }
        }
    }

    fun playNext() {
        val songs = _songList.value
        val current = _currentSong.value ?: return
        val currentIndex = songs.indexOfFirst { it.id == current.id }

        if (songs.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % songs.size
            playSong(songs[nextIndex])
        }
    }

    fun playPrevious() {
        val songs = _songList.value
        val current = _currentSong.value ?: return
        val currentIndex = songs.indexOfFirst { it.id == current.id }

        if (songs.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) songs.lastIndex else currentIndex - 1
            playSong(songs[prevIndex])
        }
    }
}