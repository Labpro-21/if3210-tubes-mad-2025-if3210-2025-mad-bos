package com.example.tubesmobdev.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubesmobdev.data.model.Song
import com.example.tubesmobdev.data.repository.PlayerPreferencesRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlayerManager
import com.example.tubesmobdev.util.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val app: Application,
    private val songRepository: SongRepository,
    private val playerRepository: PlayerPreferencesRepository,
    private val playerManager: PlayerManager
) : AndroidViewModel(app) {
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    val isPlaying = playerManager.isPlaying
    val progress = playerManager.progress

    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    val songList: StateFlow<List<Song>> = _songList

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue

    private var currentQueueIndex = -1

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    init {
        fetchSongs()
        playerManager.onClear = {
            _currentSong.value = null
        }
    }


    private fun fetchSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs().collect { songs ->
                _songList.value = songs
                Log.d("Debug", "fetchSongs: halo" )
                playerRepository.getQueue().collect { queue ->
                    _currentQueue.value = queue
                }
            }
        }
    }

    fun seekToPosition(progress: Float) {
        playerManager.seekTo(progress)
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        Log.d("Test", currentSong.value.toString())
        playerManager.play(song.filePath.toUri()) {
            playNext()
        }
        viewModelScope.launch {
            songRepository.updateLastPlayed(song.id, System.currentTimeMillis())
        }
    }
    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun clearSong() {
        playerManager.clearWithCallback()
        _currentSong.value = null
    }

    fun stopIfPlaying(song: Song) {
        if (_currentSong.value?.id == song.id) {
            clearSong()
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
        fetchSongs()
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        if (_isShuffle.value) {
            val songs = _songList.value.toMutableList()
//            songs.shuffle()
//            _currentQueue.value = songs
            _currentSong.value?.let { current ->
                currentQueueIndex = songs.indexOfFirst { it.id == current.id }.takeIf { it != -1 } ?: 0
            }
        } else {
            fetchSongs()
            _currentQueue.value = emptyList()
            currentQueueIndex = 0
        }
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.NONE
        }
    }


    private fun getNextSong(): Song? {
        return if (_isShuffle.value){
            if (_songList.value.isNotEmpty()) _songList.value.random() else null
        } else if (_currentQueue.value.isNotEmpty()) {
            Log.d("Debug", "getNextSong using queue ")
            val queue = _currentQueue.value
            if (currentQueueIndex < queue.size - 1) {
                queue[currentQueueIndex + 1]
            } else if (_repeatMode.value == RepeatMode.REPEAT_ALL){
                queue[(currentQueueIndex + 1) % queue.size]
            } else {
                viewModelScope.launch {
                    playerRepository.clearQueue()
                }
                _currentQueue.value = emptyList()
                if (_songList.value.isNotEmpty()) _songList.value.random() else null
            }
        } else {
            Log.d("Debug", "getNextSong without queue ")
            val songs = _songList.value
            val currentIndex = songs.indexOfFirst { it.id == _currentSong.value?.id }
            songs[(currentIndex + 1) % _songList.value.size]
        }
    }

    private fun getPreviousSong(): Song? {
        return if (_isShuffle.value){
            if (_songList.value.isNotEmpty()) _songList.value.random() else null
        } else if (_currentQueue.value.isNotEmpty()) {
            Log.d("Debug", "getPrevSong using queue ")
            val queue = _currentQueue.value
            if (currentQueueIndex > 0) {
                queue[currentQueueIndex - 1]
            } else {
                if (_songList.value.isNotEmpty()) _songList.value.random() else null
            }
        } else {
            Log.d("Debug", "getPrevSong without queue ")
            val songs = _songList.value
            val currentIndex = songs.indexOfFirst { it.id == _currentSong.value?.id }
            val prevIndex = if (currentIndex - 1 < 0) songs.lastIndex else currentIndex - 1
            songs[prevIndex]
        }
    }

    fun playNext() {
        fetchSongs()
        if (_repeatMode.value == RepeatMode.REPEAT_ONE) {
            _currentSong.value?.let { playSong(it) }
            return
        }
        val nextSong = getNextSong()
        if (nextSong != null) {
            if (_currentQueue.value.isNotEmpty()) {
                val queue = _currentQueue.value
                val index = queue.indexOfFirst { it.id == nextSong.id }
                if (index != -1) {
                    currentQueueIndex = index
                }
            }
            playSong(nextSong)
        } else {
            _currentSong.value?.let { playSong(it) }
        }
    }

    fun playPrevious() {
        fetchSongs()
        if (_repeatMode.value == RepeatMode.REPEAT_ONE) {
            _currentSong.value?.let { playSong(it) }
            return
        }
        val previousSong = getPreviousSong()
        if (previousSong != null) {
            if (_currentQueue.value.isNotEmpty()) {
                val queue = _currentQueue.value
                val index = queue.indexOfFirst { it.id == previousSong.id }
                if (index != -1) {
                    currentQueueIndex = index
                }
            }
            playSong(previousSong)
        } else {
            _currentSong.value?.let { playSong(it) }
        }
    }

    fun updateCurrentSongIfMatches(updatedSong: Song) {
        val current = _currentSong.value
        if (current != null && current.id == updatedSong.id) {
            _currentSong.value = updatedSong
        }
        fetchSongs()
    }

    private fun updateQueue(queue: List<Song>) {
        _currentQueue.value = queue
        viewModelScope.launch {
            playerRepository.saveQueue(queue)
        }
    }
    fun addQueue(newSong: Song) {
        Log.d("Debug", "addQueue: "+newSong.title)
        val updatedQueue = _currentQueue.value.toMutableList()
        updatedQueue.add(newSong)
        _currentQueue.value = updatedQueue
        updateQueue(updatedQueue)
        Log.d("Debug", "Updated queue: " + updatedQueue.joinToString(separator = ", ") { it.title })
    }
}
